package com.jovine360.talenthub.identity.domain.exception;

import org.springframework.security.authentication.LockedException;

/**
 * Thrown when a user account is temporarily locked due to too many failed
 * login attempts. Mapped to HTTP 401 by the global handler.
 */
public class AccountLockedException extends LockedException {

    public AccountLockedException() {
        super("Account is temporarily locked due to too many failed login attempts. Please try again later.");
    }
}
