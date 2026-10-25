package com.jovine360.talenthub.identity.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response returned after successful authentication (login or token refresh).
 */
public record AuthResponse(
        @JsonProperty("access_token")  String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("user_id")       String userId,
        String email,
        String role,
        @JsonProperty("expires_in")    long expiresIn
) {
}
