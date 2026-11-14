package com.jovine360.talenthub.identity.domain.model;

/**
 * Platform-level user roles.
 */
public enum UserRole {
    ATHLETE,
    COACH,
    PARENT,
    GUARDIAN,
    AGENT,
    SCOUT,
    ORG_ADMIN,
    ADMIN,
    SUPER_ADMIN;

    /**
     * Privileged roles can never be self-assigned. ADMIN and SUPER_ADMIN are granted by a
     * SUPER_ADMIN; ORG_ADMIN is granted by creating an organization.
     */
    public boolean isPrivileged() {
        return this == ORG_ADMIN || this == ADMIN || this == SUPER_ADMIN;
    }
}
