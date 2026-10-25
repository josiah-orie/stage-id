package com.jovine360.talenthub.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * REST request body for user login.
 */
public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {
}
