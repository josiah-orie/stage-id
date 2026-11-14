package com.jovine360.talenthub.guardian.api;

import java.time.Instant;

/**
 * A guardian, parent or agent relationship. {@code status} is {@code EXPIRED} when an ACTIVE
 * link has passed its {@code expiresAt}.
 */
public record GuardianLinkDto(
        String id,
        String guardianUserId,
        String athleteUserId,
        String relationshipType,
        String relationship,
        String permission,
        String status,
        boolean consentGiven,
        String verificationDocumentRef,
        Instant createdAt,
        Instant approvedAt,
        Instant expiresAt,
        Instant revokedAt
) {}
