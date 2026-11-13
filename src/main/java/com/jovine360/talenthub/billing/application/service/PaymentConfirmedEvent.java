package com.jovine360.talenthub.billing.application.service;

import java.math.BigDecimal;

public record PaymentConfirmedEvent(
        String transactionId,
        String subscriptionId,
        String userId,
        BigDecimal amount,
        String currency
) {}
