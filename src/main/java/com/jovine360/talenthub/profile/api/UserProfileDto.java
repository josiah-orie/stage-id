package com.jovine360.talenthub.profile.api;

import java.time.LocalDate;

/**
 * Read model for a user profile returned by the API.
 */
public record UserProfileDto(
        String id,
        String userId,
        String firstName,
        String lastName,
        String displayName,
        LocalDate dateOfBirth,
        String gender,
        String phoneNumber,
        String countryCode,
        String city,
        String bio,
        String avatarUrl,
        String role,
        boolean profileComplete
) {
}
