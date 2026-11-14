package com.jovine360.talenthub.athleteaccess;

import com.jovine360.talenthub.athleteaccess.application.service.AthleteAccessService;
import com.jovine360.talenthub.athleteaccess.domain.model.AthleteAccessGrant;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.athleteaccess.domain.model.GranteeType;
import com.jovine360.talenthub.athleteaccess.domain.repository.AthleteAccessGrantRepository;
import com.jovine360.talenthub.guardian.api.GuardianApi;
import com.jovine360.talenthub.organization.api.OrganizationApi;
import com.jovine360.talenthub.shared.domain.exception.SafeguardingViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Allow/deny matrix for the athlete access policy. No Spring context. */
class AthleteAccessPolicyTest {

    private static final String ATHLETE = "athlete-1";
    private static final String OTHER = "user-2";

    private AthleteAccessGrantRepository grants;
    private GuardianApi guardianApi;
    private OrganizationApi organizationApi;
    private AthleteAccessService policy;

    @BeforeEach
    void setUp() {
        grants = mock(AthleteAccessGrantRepository.class);
        guardianApi = mock(GuardianApi.class);
        organizationApi = mock(OrganizationApi.class);
        policy = new AthleteAccessService(grants, guardianApi, organizationApi);
    }

    @Test
    void athleteCanManageOwnData() {
        assertThat(policy.canAccess(ATHLETE, ATHLETE, List.of("ATHLETE"), AthletePermission.MANAGE)).isTrue();
    }

    @Test
    void consentedGuardianCanManage() {
        when(guardianApi.hasActiveGuardianLink(OTHER, ATHLETE)).thenReturn(true);
        assertThat(policy.canAccess(ATHLETE, OTHER, List.of("PARENT"), AthletePermission.MANAGE)).isTrue();
    }

    @Test
    void guardianWithoutActiveConsentedLinkIsDenied() {
        when(guardianApi.hasActiveGuardianLink(OTHER, ATHLETE)).thenReturn(false);
        assertThat(policy.canAccess(ATHLETE, OTHER, List.of("PARENT"), AthletePermission.MANAGE)).isFalse();
    }

    @Test
    void agentWithManageGrantCanManage() {
        stubGrant(AthletePermission.MANAGE, null);
        assertThat(policy.canAccess(ATHLETE, OTHER, List.of("AGENT"), AthletePermission.MANAGE)).isTrue();
    }

    @Test
    void viewGrantDoesNotAllowManage() {
        stubGrant(AthletePermission.VIEW_STATS, null);
        assertThat(policy.canAccess(ATHLETE, OTHER, List.of("COACH"), AthletePermission.VIEW_STATS)).isTrue();
        assertThat(policy.canAccess(ATHLETE, OTHER, List.of("COACH"), AthletePermission.MANAGE)).isFalse();
    }

    @Test
    void expiredGrantIsDenied() {
        stubGrant(AthletePermission.MANAGE, Instant.now().minusSeconds(60));
        assertThat(policy.canAccess(ATHLETE, OTHER, List.of("AGENT"), AthletePermission.MANAGE)).isFalse();
    }

    @Test
    void unrelatedUserIsDenied() {
        assertThat(policy.canAccess(ATHLETE, OTHER, List.of("SCOUT"), AthletePermission.VIEW_PROFILE)).isFalse();
        assertThatThrownBy(() -> policy.assertCanAccess(ATHLETE, OTHER, List.of("SCOUT"), AthletePermission.VIEW_PROFILE))
                .isInstanceOf(SafeguardingViolationException.class);
    }

    @Test
    void adminAndSuperAdminBypass() {
        assertThat(policy.canAccess(ATHLETE, OTHER, List.of("ADMIN"), AthletePermission.MANAGE)).isTrue();
        assertThat(policy.canAccess(ATHLETE, OTHER, List.of("SUPER_ADMIN"), AthletePermission.MANAGE)).isTrue();
    }

    @Test
    void coachOfAffiliatedOrganizationInheritsTheOrganizationGrant() {
        when(organizationApi.organizationsWithAthleteAccess("coach-1")).thenReturn(List.of("org-1"));
        AthleteAccessGrant g = new AthleteAccessGrant();
        g.setPermission(AthletePermission.VIEW_STATS);
        g.setActive(true);
        when(grants.findByAthleteUserIdAndGranteeIdAndGranteeTypeAndActiveTrue(ATHLETE, "org-1", GranteeType.ORGANIZATION))
                .thenReturn(List.of(g));

        assertThat(policy.canAccess(ATHLETE, "coach-1", List.of("COACH"), AthletePermission.VIEW_STATS)).isTrue();
        assertThat(policy.canAccess(ATHLETE, "coach-1", List.of("COACH"), AthletePermission.MANAGE)).isFalse();
        assertThat(policy.canAccess(ATHLETE, "coach-2", List.of("COACH"), AthletePermission.VIEW_STATS)).isFalse();
    }

    private void stubGrant(AthletePermission permission, Instant expiresAt) {
        AthleteAccessGrant g = new AthleteAccessGrant();
        g.setAthleteUserId(ATHLETE);
        g.setGranteeId(OTHER);
        g.setGranteeType(GranteeType.USER);
        g.setPermission(permission);
        g.setExpiresAt(expiresAt);
        g.setActive(true);
        when(grants.findByAthleteUserIdAndGranteeIdAndGranteeTypeAndActiveTrue(ATHLETE, OTHER, GranteeType.USER))
                .thenReturn(List.of(g));
    }
}
