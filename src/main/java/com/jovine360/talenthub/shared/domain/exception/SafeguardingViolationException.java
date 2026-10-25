package com.jovine360.talenthub.shared.domain.exception;

/**
 * Thrown when an operation violates a safeguarding policy for a minor athlete.
 * Maps to HTTP 403 (Forbidden) via the global exception handler.
 */
public class SafeguardingViolationException extends RuntimeException {

    private final String policyCode;

    public SafeguardingViolationException(String message) {
        super(message);
        this.policyCode = null;
    }

    public SafeguardingViolationException(String policyCode, String message) {
        super(message);
        this.policyCode = policyCode;
    }

    public String getPolicyCode() {
        return policyCode;
    }
}
