package com.jovine360.talenthub.guardian.api;

/** Published when an ACTIVE guardian relationship is revoked. */
public record GuardianLinkEndedEvent(
        String linkId,
        String guardianUserId,
        String athleteUserId,
        String relationshipType
) {}
