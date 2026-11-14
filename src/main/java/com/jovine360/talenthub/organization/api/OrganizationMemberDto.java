package com.jovine360.talenthub.organization.api;

import java.time.Instant;

public record OrganizationMemberDto(
        String id, String organizationId, String userId, String memberRole,
        String status, Instant joinedAt
) {}
