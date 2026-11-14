package com.jovine360.talenthub.scout.api;

import java.util.Optional;

/**
 * Public interface for the scout module.
 */
public interface ScoutApi {
    Optional<ScoutProfileDto> getScoutProfile(String scoutProfileId);
    boolean isVerifiedScout(String userAccountId);

    /** The scout profile id owned by this user account, if any. */
    Optional<String> findScoutProfileIdByUserId(String userAccountId);

    /** Admin listing. {@code status} null means every status. */
    org.springframework.data.domain.Page<ScoutProfileDto> listByVerificationStatus(
            String status, org.springframework.data.domain.Pageable pageable);

    /**
     * The caller's scout profile id.
     *
     * @throws com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException (404)
     *         when the user has not created a scout profile yet
     */
    String requireScoutProfileIdForUser(String userAccountId);
}
