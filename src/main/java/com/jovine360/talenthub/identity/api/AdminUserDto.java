package com.jovine360.talenthub.identity.api;

import java.time.Instant;
import java.util.List;

/** A user account as seen by platform administrators. */
public record AdminUserDto(
        String userId,
        String email,
        String role,
        List<String> roles,
        String status,
        boolean emailVerified,
        Instant createdAt,
        Instant lastLoginAt
) {}
