package com.jovine360.talenthub.athleteaccess.application.service;

import com.jovine360.talenthub.athleteaccess.api.AthleteAccessApi;
import com.jovine360.talenthub.athleteaccess.domain.model.AthleteAccessGrant;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.athleteaccess.domain.model.GranteeType;
import com.jovine360.talenthub.athleteaccess.domain.repository.AthleteAccessGrantRepository;
import com.jovine360.talenthub.shared.domain.exception.SafeguardingViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Manages and enforces access permissions for athlete profile data.
 */
@Service
@Transactional(readOnly = true)
public class AthleteAccessService implements AthleteAccessApi {

    private final AthleteAccessGrantRepository grantRepository;

    public AthleteAccessService(AthleteAccessGrantRepository grantRepository) {
        this.grantRepository = grantRepository;
    }

    @Override
    public boolean checkPermission(String athleteUserId, String granteeId,
                                   GranteeType granteeType, AthletePermission permission) {
        // Athletes always have access to their own profile
        if (athleteUserId.equals(granteeId)) return true;

        return grantRepository
                .findByAthleteUserIdAndGranteeIdAndGranteeTypeAndActiveTrue(athleteUserId, granteeId, granteeType)
                .stream()
                .filter(g -> !g.isExpired())
                .anyMatch(g -> permissionLevel(g.getPermission()) >= permissionLevel(permission));
    }

    @Override
    public void assertPermission(String athleteUserId, String granteeId,
                                 GranteeType granteeType, AthletePermission permission) {
        if (!checkPermission(athleteUserId, granteeId, granteeType, permission)) {
            throw new SafeguardingViolationException("ACCESS_DENIED",
                    "You do not have " + permission + " permission for this athlete's profile.");
        }
    }

    @Transactional
    public AthleteAccessGrant grantAccess(String athleteUserId, String granteeId,
                                          GranteeType granteeType, AthletePermission permission,
                                          Instant expiresAt) {
        AthleteAccessGrant grant = new AthleteAccessGrant();
        grant.setAthleteUserId(athleteUserId);
        grant.setGranteeId(granteeId);
        grant.setGranteeType(granteeType);
        grant.setPermission(permission);
        grant.setExpiresAt(expiresAt);
        grant.setActive(true);
        return grantRepository.save(grant);
    }

    /** Numeric rank for permission hierarchy. Higher = more access. */
    private int permissionLevel(AthletePermission p) {
        return switch (p) {
            case VIEW_PROFILE   -> 1;
            case VIEW_STATS     -> 2;
            case VIEW_DOCUMENTS -> 3;
            case CONTACT        -> 2;
            case MANAGE         -> 10;
        };
    }
}
