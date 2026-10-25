package com.jovine360.talenthub.athlete.api;

/**
 * Read model for an athlete profile.
 */
public record AthleteProfileDto(
        String id,
        String userId,
        String athleteNumber,
        String primarySportId,
        String primarySportCode,
        String position,
        Integer heightCm,
        Integer weightKg,
        String dominantFoot,
        String status,
        String organizationId,
        String nationality
) {
}
