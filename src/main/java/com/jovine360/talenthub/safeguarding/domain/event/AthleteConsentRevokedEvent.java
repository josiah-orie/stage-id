package com.jovine360.talenthub.safeguarding.domain.event;

public record AthleteConsentRevokedEvent(
        String athleteId,
        String purposeCode,
        String revokedBy
) {}
