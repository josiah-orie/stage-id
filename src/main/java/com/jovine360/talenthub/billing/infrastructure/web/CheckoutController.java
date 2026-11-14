package com.jovine360.talenthub.billing.infrastructure.web;

import com.jovine360.talenthub.billing.application.service.PaymentGatewayProvider;
import com.jovine360.talenthub.billing.domain.model.PaymentTransaction;
import com.jovine360.talenthub.billing.domain.model.TransactionStatus;
import com.jovine360.talenthub.billing.domain.repository.PaymentTransactionRepository;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import com.jovine360.talenthub.subscription.api.SubscriptionApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/** Starts payment for a PENDING subscription. The payment webhook then activates it. */
@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Checkout", description = "Pay for a subscription")
public class CheckoutController {

    private final SubscriptionApi subscriptionApi;
    private final PaymentGatewayProvider gateway;
    private final PaymentTransactionRepository transactionRepository;
    private final String callbackUrl;
    private final String providerCode;

    public CheckoutController(SubscriptionApi subscriptionApi, PaymentGatewayProvider gateway,
                              PaymentTransactionRepository transactionRepository,
                              @Value("${payment.callback-url:https://app.talenthub.io/billing/callback}") String callbackUrl,
                              @Value("${payment.provider:paystack}") String providerCode) {
        this.subscriptionApi = subscriptionApi;
        this.gateway = gateway;
        this.transactionRepository = transactionRepository;
        this.callbackUrl = callbackUrl;
        this.providerCode = providerCode.toUpperCase();
    }

    public record CheckoutResponse(String transactionId, String reference, String authorizationUrl,
                                   String accessCode) {}

    @Operation(summary = "Start payment for a PENDING subscription; redirect the user to authorizationUrl")
    @PostMapping("/{id}/checkout")
    @Transactional
    public ResponseEntity<CheckoutResponse> checkout(@PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        SubscriptionApi.SubscriptionSummary sub = subscriptionApi.loadOwned(id, principal.userId(), principal.roles());
        if (!"PENDING".equals(sub.status())) {
            throw new InvalidStateTransitionException("UserSubscription", sub.status(), "CHECKOUT");
        }
        if (sub.pricePerMonth() == null || sub.pricePerMonth().signum() == 0) {
            throw new IllegalArgumentException("This plan is free and needs no payment");
        }
        PaymentGatewayProvider.PaymentInitResponse init =
                gateway.initializeTransaction(principal.userId(), sub.pricePerMonth(), sub.currency(), callbackUrl);

        PaymentTransaction tx = new PaymentTransaction();
        tx.setSubscriptionId(sub.id());
        tx.setUserId(principal.userId());
        tx.setProviderCode(providerCode);
        tx.setProviderReference(init.reference());
        tx.setAmount(sub.pricePerMonth());
        tx.setCurrency(sub.currency());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setInitiatedAt(Instant.now());
        PaymentTransaction saved = transactionRepository.save(tx);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CheckoutResponse(
                saved.getId(), init.reference(), init.authorizationUrl(), init.accessCode()));
    }
}
