package com.jovine360.talenthub.scout.api;

import java.util.Optional;

/**
 * Public interface for the scout module.
 */
public interface ScoutApi {
    Optional<ScoutProfileDto> getScoutProfile(String scoutProfileId);
    boolean isVerifiedScout(String userAccountId);
}
