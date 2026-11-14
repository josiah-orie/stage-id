package com.jovine360.talenthub.subscription.api;

import com.jovine360.talenthub.subscription.domain.model.BillingPeriod;
import com.jovine360.talenthub.subscription.domain.model.PlanTargetRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Request bodies for SUPER_ADMIN plan management. */
public final class PlanRequests {

    private PlanRequests() {}

    public record PriceRequest(
            @NotBlank @Size(min = 3, max = 3) String currency,
            @NotNull @DecimalMin("0.00") BigDecimal amount,
            @NotNull BillingPeriod billingPeriod) {}

    /**
     * @param entitlements limit per entitlement key, e.g. {@code {"MAX_WATCHLISTS": 10}}. Keys not
     *                     listed are unlimited for subscribers to this plan.
     */
    public record CreatePlanRequest(
            @NotBlank @Size(max = 50) @Pattern(regexp = "[A-Z0-9_]+") String code,
            @NotBlank @Size(max = 100) String name,
            @NotNull PlanTargetRole targetRole,
            @NotNull @DecimalMin("0.00") BigDecimal pricePerMonth,
            @Size(min = 3, max = 3) String currency,
            Map<String, Long> entitlements,
            List<@Valid PriceRequest> prices) {}

    /** Partial update; null fields are left unchanged. {@code entitlements} and {@code prices} replace the old values. */
    public record UpdatePlanRequest(
            @Size(max = 100) String name,
            @DecimalMin("0.00") BigDecimal pricePerMonth,
            @Size(min = 3, max = 3) String currency,
            Map<String, Long> entitlements,
            List<@Valid PriceRequest> prices,
            Boolean active) {}
}
