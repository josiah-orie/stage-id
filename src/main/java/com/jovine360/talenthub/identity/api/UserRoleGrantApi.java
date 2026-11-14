package com.jovine360.talenthub.identity.api;

/**
 * System-initiated role changes, used by other modules (for example, creating an organization
 * makes its creator an ORG_ADMIN). Not reachable from any self-service endpoint.
 */
public interface UserRoleGrantApi {

    /** Adds the role as an additional role if the user does not already hold it. */
    void grantRole(String userId, String role);
}
