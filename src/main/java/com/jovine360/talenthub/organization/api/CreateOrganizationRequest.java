package com.jovine360.talenthub.organization.api;

import com.jovine360.talenthub.organization.domain.model.OrgType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull OrgType orgType,
        @Size(max = 2) String countryCode,
        @Size(max = 100) String city,
        @Size(max = 1000) String description
) {}
