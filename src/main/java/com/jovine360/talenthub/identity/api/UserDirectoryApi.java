package com.jovine360.talenthub.identity.api;

import java.util.List;

/**
 * Read-only lookups on user accounts for other modules.
 */
public interface UserDirectoryApi {

    /** True if the user exists and holds the given role (primary or additional). */
    boolean userHasRole(String userId, String role);

    /** All role names held by the user, or an empty list when the user does not exist. */
    List<String> rolesOf(String userId);
}
