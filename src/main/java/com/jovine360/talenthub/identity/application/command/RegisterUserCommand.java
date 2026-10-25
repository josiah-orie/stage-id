package com.jovine360.talenthub.identity.application.command;

import com.jovine360.talenthub.identity.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Command to register a new user.
 */
public record RegisterUserCommand(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotNull UserRole role
) {
}
