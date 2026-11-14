package com.jovine360.talenthub.identity.domain.exception;

import org.springframework.security.authentication.DisabledException;

/** Thrown at login or refresh for a SUSPENDED or DEACTIVATED account. Mapped to HTTP 401. */
public class AccountSuspendedException extends DisabledException {

    public AccountSuspendedException() {
        super("This account is suspended. Contact support.");
    }
}
