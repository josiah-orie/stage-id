package com.jovine360.talenthub.guardian.api;

import java.time.Instant;

/** Published when a guardian relationship becomes ACTIVE. */
public record GuardianLinkActivatedEvent(
        String linkId,
        String guardianUserId,
        String athleteUserId,
        String relationshipType,
        String permission,
        Instant expiresAt
) {}
