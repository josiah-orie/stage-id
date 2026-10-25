package com.jovine360.talenthub.profile.api;

/**
 * Public interface for the profile module — used by other modules that need
 * to read profile data without going through HTTP.
 */
public interface UserProfileApi {

    /**
     * Returns the profile DTO for the given user ID, or null if not found.
     */
    UserProfileDto getProfileByUserId(String userId);

    /**
     * Returns true if the given user has completed their profile.
     */
    boolean isProfileComplete(String userId);
}
