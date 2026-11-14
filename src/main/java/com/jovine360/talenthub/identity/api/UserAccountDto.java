package com.jovine360.talenthub.identity.api;

import java.util.List;

/** The caller's own account, as returned by {@code GET /users/me}. */
public record UserAccountDto(
        String userId,
        String email,
        String role,
        List<String> roles,
        String status,
        boolean emailVerified
) {}
