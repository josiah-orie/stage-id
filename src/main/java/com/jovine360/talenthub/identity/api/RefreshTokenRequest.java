package com.jovine360.talenthub.identity.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * REST request body for token refresh.
 */
public record RefreshTokenRequest(
        @JsonProperty("refresh_token") @NotBlank String refreshToken
) {
}
