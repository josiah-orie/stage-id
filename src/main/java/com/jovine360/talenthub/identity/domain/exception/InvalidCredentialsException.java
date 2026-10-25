package com.jovine360.talenthub.identity.domain.exception;

/**
 * Thrown when login credentials are invalid. Uses a generic message
 * to prevent user enumeration.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
