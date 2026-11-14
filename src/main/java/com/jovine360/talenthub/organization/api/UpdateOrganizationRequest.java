package com.jovine360.talenthub.organization.api;

import jakarta.validation.constraints.Size;

/** Partial update. Null fields are left unchanged. */
public record UpdateOrganizationRequest(
        @Size(max = 200) String name,
        @Size(max = 2) String countryCode,
        @Size(max = 100) String city,
        @Size(max = 1000) String description,
        @Size(max = 512) String logoUrl
) {}
