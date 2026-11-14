package com.jovine360.talenthub.subscription.api;

import java.math.BigDecimal;
import java.util.Collection;

/** Subscription operations other modules (billing) may call. */
public interface SubscriptionApi {

    record SubscriptionSummary(String id, String subscriberId, String status, String planCode,
                               BigDecimal pricePerMonth, String currency) {}

    /**
     * Loads a subscription the caller may act on: their own, their scout profile's, an organization
     * they administer, or any subscription for a platform admin.
     *
     * @throws com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException 404 if missing
     * @throws SecurityException 403 if it belongs to someone else
     */
    SubscriptionSummary loadOwned(String subscriptionId, String userId, Collection<String> roles);

    /** PENDING → ACTIVE after a confirmed payment. Ignored if already active. */
    void activate(String subscriptionId);
}
