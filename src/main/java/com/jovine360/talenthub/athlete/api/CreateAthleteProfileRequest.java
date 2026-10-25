package com.jovine360.talenthub.athlete.api;

import jakarta.validation.constraints.Size;

/**
 * Request to create or update an athlete's sport-specific profile.
 */
public record CreateAthleteProfileRequest(
        String primarySportCode,
        @Size(max = 100) String position,
        Integer heightCm,
        Integer weightKg,
        @Size(max = 10) String dominantFoot,
        @Size(max = 2)  String nationality
) {
}
