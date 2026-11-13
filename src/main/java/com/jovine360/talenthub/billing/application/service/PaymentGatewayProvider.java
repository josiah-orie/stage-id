package com.jovine360.talenthub.billing.application.service;

import java.math.BigDecimal;

/**
 * Abstraction over payment gateways (Paystack, Stripe, etc.).
 */
public interface PaymentGatewayProvider {
    PaymentInitResponse initializeTransaction(String userId, BigDecimal amount,
                                               String currency, String callbackUrl);
    PaymentVerifyResponse verifyTransaction(String reference);
    void processRefund(String reference, BigDecimal amount);

    record PaymentInitResponse(String reference, String authorizationUrl, String accessCode) {}
    record PaymentVerifyResponse(String reference, String status, BigDecimal amount, String currency) {}
}
