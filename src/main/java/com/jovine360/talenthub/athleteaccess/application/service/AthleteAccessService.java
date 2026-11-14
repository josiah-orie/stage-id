package com.jovine360.talenthub.athleteaccess.application.service;

import com.jovine360.talenthub.athleteaccess.api.AthleteAccessApi;
import com.jovine360.talenthub.athleteaccess.api.AthleteAccessPolicy;
import com.jovine360.talenthub.athleteaccess.domain.model.AthleteAccessGrant;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.athleteaccess.domain.model.GranteeType;
import com.jovine360.talenthub.athleteaccess.domain.repository.AthleteAccessGrantRepository;
import com.jovine360.talenthub.guardian.api.GuardianApi;
import com.jovine360.talenthub.organization.api.OrganizationApi;
import com.jovine360.talenthub.shared.domain.exception.SafeguardingViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;

/**
 * Manages and enforces access permissions for athlete profile data.
 */
@Service
@Transactional(readOnly = true)
public class AthleteAccessService implements AthleteAccessApi, AthleteAccessPolicy {

    private final AthleteAccessGrantRepository grantRepository;

    private final GuardianApi guardianApi;
    private final OrganizationApi organizationApi;

    public AthleteAccessService(AthleteAccessGrantRepository grantRepository, GuardianApi guardianApi,
                                OrganizationApi organizationApi) {
        this.grantRepository = grantRepository;
        this.guardianApi = guardianApi;
        this.organizationApi = organizationApi;
    }

    @Override
    public boolean checkPermission(String athleteUserId, String granteeId,
                                   GranteeType granteeType, AthletePermission permission) {
        // Athletes always have access to their own profile
        if (athleteUserId.equals(granteeId)) return true;

        // A linked parent or legal guardian has full management access
        if (granteeType == GranteeType.USER && guardianApi.hasActiveGuardianLink(granteeId, athleteUserId)) {
            return true;
        }

        if (hasGrant(athleteUserId, granteeId, granteeType, permission)) {
            return true;
        }

        // Staff of an affiliated organization inherit the organization's grant
        if (granteeType == GranteeType.USER) {
            return organizationApi.organizationsWithAthleteAccess(granteeId).stream()
                    .anyMatch(orgId -> hasGrant(athleteUserId, orgId, GranteeType.ORGANIZATION, permission));
        }
        return false;
    }

    private boolean hasGrant(String athleteUserId, String granteeId, GranteeType granteeType,
                             AthletePermission permission) {
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

    @Override
    public boolean canAccess(String athleteUserId, String actorId,
                             Collection<String> actorRoles, AthletePermission permission) {
        if (actorRoles != null
                && (actorRoles.contains("ADMIN") || actorRoles.contains("SUPER_ADMIN"))) {
            return true;
        }
        return checkPermission(athleteUserId, actorId, GranteeType.USER, permission);
    }

    @Override
    public void assertCanAccess(String athleteUserId, String actorId,
                                Collection<String> actorRoles, AthletePermission permission) {
        if (!canAccess(athleteUserId, actorId, actorRoles, permission)) {
            throw new SafeguardingViolationException("ACCESS_DENIED",
                    "You do not have " + permission + " permission for this athlete.");
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

    /** Deactivates every active grant the grantee holds on the athlete. */
    @Transactional
    public void revokeAccess(String athleteUserId, String granteeId, GranteeType granteeType) {
        grantRepository
                .findByAthleteUserIdAndGranteeIdAndGranteeTypeAndActiveTrue(athleteUserId, granteeId, granteeType)
                .forEach(g -> {
                    g.setActive(false);
                    grantRepository.save(g);
                });
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
