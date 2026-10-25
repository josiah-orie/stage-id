package com.jovine360.talenthub.athlete.api;

import java.util.Optional;

/**
 * Public interface for the athlete module — cross-module access without HTTP.
 */
public interface AthleteApi {
    Optional<AthleteProfileDto> findByUserId(String userId);
    Optional<AthleteProfileDto> findByAthleteNumber(String athleteNumber);
    boolean isAthleteUser(String userId);
}
