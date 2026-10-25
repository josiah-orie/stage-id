package com.jovine360.talenthub.organization.api;

import java.util.Optional;

public interface OrganizationApi {
    Optional<OrganizationDto> findById(String orgId);
    Optional<OrganizationDto> findByCode(String code);
    boolean orgExists(String orgId);
}
