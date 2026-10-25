package com.jovine360.talenthub.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * REST request body to initiate a password reset.
 */
public record PasswordResetRequest(
        @Email @NotBlank String email
) {
}
