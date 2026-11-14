package com.jovine360.talenthub.identity.domain.exception;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Thrown when login credentials are invalid. Uses a generic message
 * to prevent user enumeration. Mapped to HTTP 401 by the global handler.
 */
public class InvalidCredentialsException extends BadCredentialsException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
