package com.jovine360.talenthub.identity.application.command;

import jakarta.validation.constraints.NotBlank;

/**
 * Command to exchange a valid refresh token for new JWT tokens.
 */
public record RefreshTokenCommand(
        @NotBlank String refreshToken
) {
}
