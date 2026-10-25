package com.jovine360.talenthub.shared.domain.exception;

/**
 * Thrown when a user or organisation has exceeded a subscription entitlement limit.
 * Maps to HTTP 402 (Payment Required) or 403 (Forbidden) depending on context.
 */
public class EntitlementLimitExceededException extends RuntimeException {

    private final String entitlementKey;
    private final long limit;
    private final long current;

    public EntitlementLimitExceededException(String entitlementKey, long limit, long current) {
        super("Entitlement limit exceeded for '" + entitlementKey
                + "': current=" + current + ", limit=" + limit);
        this.entitlementKey = entitlementKey;
        this.limit = limit;
        this.current = current;
    }

    public EntitlementLimitExceededException(String message) {
        super(message);
        this.entitlementKey = null;
        this.limit = -1;
        this.current = -1;
    }

    public String getEntitlementKey() {
        return entitlementKey;
    }

    public long getLimit() {
        return limit;
    }

    public long getCurrent() {
        return current;
    }
}
