package com.jovine360.talenthub.organization.api;

import java.util.List;
import java.util.Optional;

public interface OrganizationApi {
    Optional<OrganizationDto> findById(String orgId);
    Optional<OrganizationDto> findByCode(String code);
    boolean orgExists(String orgId);

    /** True if the user is an ACTIVE member of the organization. */
    boolean isMember(String orgId, String userId);

    /** True if the user is an ACTIVE OWNER or ADMIN member of the organization. */
    boolean isOrganizationAdmin(String orgId, String userId);

    /**
     * Organizations where the user is an ACTIVE member whose role works with athletes
     * (OWNER, ADMIN, COACH, ATHLETE_MANAGER). Used for access decisions on affiliated athletes.
     */
    List<String> organizationsWithAthleteAccess(String userId);
}
