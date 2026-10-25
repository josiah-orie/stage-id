package com.jovine360.talenthub.identity.api;

import com.jovine360.talenthub.identity.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * REST request body for user registration.
 */
public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotNull UserRole role
) {
}
