package com.jovine360.talenthub.organization.api;

import java.time.Instant;

public record OrganizationDto(
        String id, String code, String name, String orgType,
        String countryCode, String city, String adminUserId,
        String logoUrl, String status, String description,
        Instant verifiedAt, String rejectionReason
) {}
