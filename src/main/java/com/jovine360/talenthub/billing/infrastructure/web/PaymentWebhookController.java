package com.jovine360.talenthub.billing.infrastructure.web;

import com.jovine360.talenthub.billing.application.service.PaymentConfirmedEvent;
import com.jovine360.talenthub.billing.application.service.PaymentGatewayProvider;
import com.jovine360.talenthub.billing.domain.model.PaymentProviderEvent;
import com.jovine360.talenthub.billing.domain.model.PaymentTransaction;
import com.jovine360.talenthub.billing.domain.model.TransactionStatus;
import com.jovine360.talenthub.billing.domain.repository.PaymentProviderEventRepository;
import com.jovine360.talenthub.billing.domain.repository.PaymentTransactionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/v1/payments/webhooks")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    @Value("${payment.paystack.secret-key:sk_test_placeholder}")
    private String paystackSecret;

    private final PaymentProviderEventRepository eventRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentGatewayProvider paymentGatewayProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    public PaymentWebhookController(PaymentProviderEventRepository eventRepository,
                                     PaymentTransactionRepository transactionRepository,
                                     PaymentGatewayProvider paymentGatewayProvider,
                                     ApplicationEventPublisher eventPublisher,
                                     MeterRegistry meterRegistry) {
        this.eventRepository = eventRepository;
        this.transactionRepository = transactionRepository;
        this.paymentGatewayProvider = paymentGatewayProvider;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping("/{provider}")
    public ResponseEntity<String> handleWebhook(
            @PathVariable String provider,
            @RequestHeader(value = "X-Paystack-Signature", required = false) String signature,
            @RequestBody String rawBody) {

        if ("paystack".equalsIgnoreCase(provider)) {
            if (!verifyPaystackSignature(rawBody, signature)) {
                return ResponseEntity.status(401).body("Invalid signature");
            }
        }

        // Extract event ID for idempotency (simplified: use hash of payload)
        String eventId = Integer.toHexString(rawBody.hashCode());
        if (eventRepository.findByProviderCodeAndProviderEventId(provider.toUpperCase(), eventId).isPresent()) {
            log.info("Duplicate webhook event: {}", eventId);
            return ResponseEntity.ok("DUPLICATE");
        }

        PaymentProviderEvent providerEvent = new PaymentProviderEvent();
        providerEvent.setProviderCode(provider.toUpperCase());
        providerEvent.setProviderEventId(eventId);
        providerEvent.setPayloadJson(rawBody);
        providerEvent.setReceivedAt(Instant.now());
        providerEvent.setStatus("RECEIVED");
        eventRepository.save(providerEvent);

        meterRegistry.counter("payment.webhooks.processed.total").increment();
        processWebhookAsync(providerEvent);
        return ResponseEntity.ok("OK");
    }

    private void processWebhookAsync(PaymentProviderEvent providerEvent) {
        try {
            // Extract reference from payload (simplified)
            String reference = extractReference(providerEvent.getPayloadJson());
            if (reference == null) return;

            PaymentGatewayProvider.PaymentVerifyResponse verify =
                    paymentGatewayProvider.verifyTransaction(reference);

            if ("success".equalsIgnoreCase(verify.status())) {
                transactionRepository.findByProviderReference(reference).ifPresent(tx -> {
                    tx.setStatus(TransactionStatus.COMPLETED);
                    tx.setCompletedAt(Instant.now());
                    transactionRepository.save(tx);
                    eventPublisher.publishEvent(new PaymentConfirmedEvent(
                            tx.getId(), tx.getSubscriptionId(), tx.getUserId(),
                            tx.getAmount(), tx.getCurrency()));
                });
            }

            providerEvent.setStatus("PROCESSED");
            providerEvent.setProcessedAt(Instant.now());
            eventRepository.save(providerEvent);
        } catch (Exception e) {
            log.error("Webhook processing failed: {}", e.getMessage());
            providerEvent.setStatus("FAILED");
            eventRepository.save(providerEvent);
        }
    }

    private String extractReference(String payload) {
        // Very simplified extraction - real impl would parse JSON
        int idx = payload.indexOf("\"reference\":");
        if (idx < 0) return null;
        int start = payload.indexOf('"', idx + 13) + 1;
        int end = payload.indexOf('"', start);
        return (start > 0 && end > start) ? payload.substring(start, end) : null;
    }

    private boolean verifyPaystackSignature(String rawBody, String signature) {
        if (signature == null || signature.isBlank()) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(paystackSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);
            return computed.equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.warn("Signature verification error: {}", e.getMessage());
            return false;
        }
    }
}
