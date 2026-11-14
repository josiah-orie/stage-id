package com.jovine360.talenthub.subscription.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AdminPlanDto(
        String id, String code, String name, String targetRole,
        BigDecimal pricePerMonth, String currency, boolean active,
        Map<String, Long> entitlements, List<Price> prices
) {
    public record Price(String id, String currency, BigDecimal amount, String billingPeriod, boolean active) {}
}
