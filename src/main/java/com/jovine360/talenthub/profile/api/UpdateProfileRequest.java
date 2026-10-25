package com.jovine360.talenthub.profile.api;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request body for updating a user profile.
 */
public record UpdateProfileRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 150) String displayName,
        LocalDate dateOfBirth,
        @Size(max = 20)  String gender,
        @Size(max = 30)  String phoneNumber,
        @Size(max = 2)   String countryCode,
        @Size(max = 100) String city,
        @Size(max = 500) String bio
) {
}
