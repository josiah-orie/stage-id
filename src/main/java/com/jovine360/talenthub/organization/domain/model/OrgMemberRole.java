package com.jovine360.talenthub.organization.domain.model;

/** A member's role inside one organization (separate from platform roles). */
public enum OrgMemberRole {
    /** Registered the organization. Full control, cannot be removed. */
    OWNER,
    /** Manages members, affiliations and settings. */
    ADMIN,
    COACH,
    SCOUT,
    /** Requests and manages athlete affiliations. */
    ATHLETE_MANAGER,
    /** Read-only access to the organization. */
    STAFF;

    public boolean canManageOrganization() {
        return this == OWNER || this == ADMIN;
    }

    public boolean canManageAthletes() {
        return this == OWNER || this == ADMIN || this == ATHLETE_MANAGER;
    }
}
