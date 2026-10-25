package com.jovine360.talenthub.organization.api;

public record OrganizationDto(
        String id, String code, String name, String orgType,
        String countryCode, String city, String adminUserId,
        String logoUrl, String status
) {}
