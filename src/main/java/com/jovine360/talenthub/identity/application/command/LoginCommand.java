package com.jovine360.talenthub.identity.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Command to authenticate a user and obtain JWT tokens.
 */
public record LoginCommand(
        @Email @NotBlank String email,
        @NotBlank String password
) {
}
