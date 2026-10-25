package com.jovine360.talenthub.identity.domain.exception;

/**
 * Thrown when a user account is temporarily locked due to too many failed
 * login attempts.
 */
public class AccountLockedException extends RuntimeException {
    public AccountLockedException() {
        super("Account is temporarily locked due to too many failed login attempts. Please try again later.");
    }
}
