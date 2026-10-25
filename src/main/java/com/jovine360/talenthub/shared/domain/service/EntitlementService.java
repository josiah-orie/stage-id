package com.jovine360.talenthub.shared.domain.service;

/**
 * Cross-module interface for checking subscription entitlements.
 * Stubbed in Phase 1 with free-tier defaults; replaced with full enforcement in Phase 4.
 */
public interface EntitlementService {

    /**
     * Returns the numeric limit for the given entitlement key for a user/org.
     * Returns {@code Long.MAX_VALUE} for unlimited.
     *
     * @param subscriberId the userId or orgId
     * @param key          the entitlement key (e.g. "max_athletes", "max_scouts")
     */
    long getLimit(String subscriberId, String key);

    /**
     * Checks whether the subscriber is within the limit for the given key.
     *
     * @param subscriberId the userId or orgId
     * @param key          the entitlement key
     * @param current      the current usage count
     * @return true if usage is within the allowed limit
     */
    boolean isWithinLimit(String subscriberId, String key, long current);

    /**
     * Throws {@link com.jovine360.talenthub.shared.domain.exception.EntitlementLimitExceededException}
     * if the current usage has reached or exceeded the limit.
     */
    void assertWithinLimit(String subscriberId, String key, long current);

    /**
     * Returns true if the subscriber has an active subscription plan.
     */
    boolean hasActivePlan(String subscriberId);
}
