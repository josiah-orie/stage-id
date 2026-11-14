package com.jovine360.talenthub.organization.api;

import java.time.Instant;

public record AthleteAffiliationDto(
        String id, String organizationId, String athleteUserId, String permission,
        String status, String requestedBy, Instant createdAt, Instant decidedAt, Instant endedAt
) {}
