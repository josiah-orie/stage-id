package com.jovine360.talenthub.athleteaccess.api;

import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.athleteaccess.domain.model.GranteeType;

/**
 * Public interface for the athlete access module.
 */
public interface AthleteAccessApi {

    /**
     * Returns true if the grantee has the specified permission on the athlete's profile.
     */
    boolean checkPermission(String athleteUserId, String granteeId,
                            GranteeType granteeType, AthletePermission permission);

    /**
     * Throws {@link com.jovine360.talenthub.shared.domain.exception.SafeguardingViolationException}
     * if permission is denied.
     */
    void assertPermission(String athleteUserId, String granteeId,
                          GranteeType granteeType, AthletePermission permission);
}
