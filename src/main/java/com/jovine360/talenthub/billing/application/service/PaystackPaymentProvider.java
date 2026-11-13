package com.jovine360.talenthub.billing.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Paystack payment provider implementation.
 * Calls https://api.paystack.co with Bearer token auth.
 */
@Service
public class PaystackPaymentProvider implements PaymentGatewayProvider {

    private static final Logger log = LoggerFactory.getLogger(PaystackPaymentProvider.class);
    private static final String BASE_URL = "https://api.paystack.co";

    @Value("${payment.paystack.secret-key:sk_test_placeholder}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public PaymentInitResponse initializeTransaction(String userId, BigDecimal amount,
                                                      String currency, String callbackUrl) {
        HttpHeaders headers = buildHeaders();
        Map<String, Object> body = Map.of(
                "email", userId + "@talenthub.internal",
                "amount", amount.multiply(BigDecimal.valueOf(100)).longValue(), // kobo
                "currency", currency,
                "callback_url", callbackUrl
        );
        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    BASE_URL + "/transaction/initialize",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class);
            Map<String, Object> data = (Map<String, Object>) resp.getBody().get("data");
            return new PaymentInitResponse(
                    (String) data.get("reference"),
                    (String) data.get("authorization_url"),
                    (String) data.get("access_code"));
        } catch (Exception e) {
            log.error("Paystack init failed: {}", e.getMessage());
            throw new RuntimeException("Payment initialization failed", e);
        }
    }

    @Override
    public PaymentVerifyResponse verifyTransaction(String reference) {
        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    BASE_URL + "/transaction/verify/" + reference,
                    HttpMethod.GET,
                    new HttpEntity<>(buildHeaders()),
                    Map.class);
            Map<String, Object> data = (Map<String, Object>) resp.getBody().get("data");
            String status = (String) data.get("status");
            Number amountKobo = (Number) data.get("amount");
            String currency = (String) data.get("currency");
            BigDecimal amount = BigDecimal.valueOf(amountKobo.longValue()).divide(BigDecimal.valueOf(100));
            return new PaymentVerifyResponse(reference, status, amount, currency);
        } catch (Exception e) {
            log.error("Paystack verify failed: {}", e.getMessage());
            throw new RuntimeException("Payment verification failed", e);
        }
    }

    @Override
    public void processRefund(String reference, BigDecimal amount) {
        HttpHeaders headers = buildHeaders();
        Map<String, Object> body = Map.of(
                "transaction", reference,
                "amount", amount.multiply(BigDecimal.valueOf(100)).longValue()
        );
        try {
            restTemplate.exchange(BASE_URL + "/refund", HttpMethod.POST,
                    new HttpEntity<>(body, headers), Map.class);
        } catch (Exception e) {
            log.error("Paystack refund failed: {}", e.getMessage());
            throw new RuntimeException("Refund failed", e);
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);
        return headers;
    }
}
