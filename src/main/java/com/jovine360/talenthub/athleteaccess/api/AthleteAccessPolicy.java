package com.jovine360.talenthub.athleteaccess.api;

import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;

import java.util.Collection;

/**
 * Single place for deciding whether a caller may act on an athlete's data.
 *
 * <p>Access is granted when the caller is: ADMIN or SUPER_ADMIN; the athlete themself;
 * an ACTIVE, consented guardian or parent of the athlete; or holds an active, unexpired
 * access grant of at least the requested permission level.
 */
public interface AthleteAccessPolicy {

    boolean canAccess(String athleteUserId, String actorId,
                      Collection<String> actorRoles, AthletePermission permission);

    /**
     * @throws com.jovine360.talenthub.shared.domain.exception.SafeguardingViolationException
     *         (HTTP 403) when {@link #canAccess} is false
     */
    void assertCanAccess(String athleteUserId, String actorId,
                         Collection<String> actorRoles, AthletePermission permission);
}
