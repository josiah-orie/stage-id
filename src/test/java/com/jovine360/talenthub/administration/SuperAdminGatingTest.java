package com.jovine360.talenthub.administration;

import com.jovine360.talenthub.activityconfiguration.application.service.ActivityConfigurationService;
import com.jovine360.talenthub.activityconfiguration.infrastructure.web.AdminActivityConfigController;
import com.jovine360.talenthub.administration.api.AdminController;
import com.jovine360.talenthub.administration.infrastructure.persistence.PlatformMetricsQuery;
import com.jovine360.talenthub.identity.api.AdminUserDto;
import com.jovine360.talenthub.identity.api.SuperAdminUserController;
import com.jovine360.talenthub.identity.api.UserAdministrationApi;
import com.jovine360.talenthub.identity.domain.model.UserRole;
import com.jovine360.talenthub.scout.api.ScoutApi;
import com.jovine360.talenthub.shared.domain.service.AuditTrail;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import com.jovine360.talenthub.sport.api.AdminSportController;
import com.jovine360.talenthub.sport.api.SportAdminDtos;
import com.jovine360.talenthub.sport.application.service.SportAdministrationService;
import com.jovine360.talenthub.sport.api.SportDto;
import com.jovine360.talenthub.subscription.api.AdminPlanController;
import com.jovine360.talenthub.subscription.api.AdminPlanDto;
import com.jovine360.talenthub.subscription.api.PlanRequests;
import com.jovine360.talenthub.subscription.application.service.PlanAdministrationService;
import com.jovine360.talenthub.subscription.domain.model.PlanTargetRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Exercises the real @PreAuthorize rules on the admin controllers with method security enabled.
 * Services are mocks; no database or web server.
 */
@SpringJUnitConfig(SuperAdminGatingTest.Config.class)
class SuperAdminGatingTest {

    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean AuditTrail auditTrail() { return mock(AuditTrail.class); }
        @Bean UserAdministrationApi userAdministration() { return mock(UserAdministrationApi.class); }
        @Bean PlanAdministrationService planService() { return mock(PlanAdministrationService.class); }
        @Bean SportAdministrationService sportService() { return mock(SportAdministrationService.class); }
        @Bean ActivityConfigurationService configService() { return mock(ActivityConfigurationService.class); }
        @Bean SuperAdminUserController superAdminUserController(UserAdministrationApi u, AuditTrail a) { return new SuperAdminUserController(u, a); }
        @Bean AdminPlanController adminPlanController(PlanAdministrationService p, AuditTrail a) { return new AdminPlanController(p, a); }
        @Bean AdminSportController adminSportController(SportAdministrationService s, AuditTrail a) { return new AdminSportController(s, a); }
        @Bean AdminActivityConfigController adminActivityConfigController(ActivityConfigurationService c, AuditTrail a) { return new AdminActivityConfigController(c, a); }
        @Bean AdminController adminController(UserAdministrationApi u, AuditTrail a) {
            return new AdminController(u, mock(ScoutApi.class), mock(PlatformMetricsQuery.class), a);
        }
    }

    @Autowired SuperAdminUserController superAdminUsers;
    @Autowired AdminPlanController plans;
    @Autowired AdminSportController sports;
    @Autowired AdminActivityConfigController activityConfig;
    @Autowired AdminController admin;
    @Autowired UserAdministrationApi userAdministration;
    @Autowired PlanAdministrationService planService;
    @Autowired SportAdministrationService sportService;
    @Autowired AuditTrail audit;

    private AuthenticatedPrincipal loginAs(String id, String role) {
        AuthenticatedPrincipal p = AuthenticatedPrincipal.of(id, id + "@x.com", List.of(role));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()));
        return p;
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
        reset(audit, userAdministration, planService, sportService);
    }

    private static final PlanRequests.CreatePlanRequest PLAN = new PlanRequests.CreatePlanRequest(
            "SCOUT_ELITE", "Scout Elite", PlanTargetRole.SCOUT, new BigDecimal("49.00"), "USD",
            Map.of("MAX_WATCHLISTS", 50L), null);

    @Test
    void adminIsRefusedEverySuperAdminOperation() {
        AuthenticatedPrincipal p = loginAs("admin-1", "ADMIN");
        assertThatThrownBy(() -> plans.create(PLAN, p)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> plans.deactivate("plan-1", p)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> superAdminUsers.changeRole("u1",
                new SuperAdminUserController.ChangeRoleRequest(UserRole.ADMIN), p)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> superAdminUsers.createAdmin(
                new SuperAdminUserController.CreateAdminRequest("new@x.com", "Password123!"), p))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> sports.create(new SportAdminDtos.CreateSportRequest("RG7", "Rugby 7s", null), p))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> activityConfig.publishSchema("v1", p)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(planService, sportService);
    }

    @Test
    void superAdminCanCreatePlanAndItIsAudited() {
        AuthenticatedPrincipal p = loginAs("super-1", "SUPER_ADMIN");
        when(planService.create(any())).thenReturn(new AdminPlanDto("plan-1", "SCOUT_ELITE", "Scout Elite", "SCOUT",
                new BigDecimal("49.00"), "USD", true, Map.of("MAX_WATCHLISTS", 50L), List.of()));

        assertThat(plans.create(PLAN, p).getStatusCode().value()).isEqualTo(201);
        verify(audit).record(eq("super-1"), any(), eq("PLAN_CREATED"), eq("SubscriptionPlan"), eq("plan-1"), any());
    }

    @Test
    void superAdminRoleChangeIsAudited() {
        AuthenticatedPrincipal p = loginAs("super-1", "SUPER_ADMIN");
        AdminUserDto before = new AdminUserDto("u1", "u1@x.com", "ATHLETE", List.of("ATHLETE"), "ACTIVE", true, null, null);
        AdminUserDto after = new AdminUserDto("u1", "u1@x.com", "ADMIN", List.of("ADMIN"), "ACTIVE", true, null, null);
        when(userAdministration.getUser("u1")).thenReturn(before);
        when(userAdministration.changePrimaryRole("u1", "ADMIN", "super-1")).thenReturn(after);

        assertThat(superAdminUsers.changeRole("u1", new SuperAdminUserController.ChangeRoleRequest(UserRole.ADMIN), p)
                .getBody().role()).isEqualTo("ADMIN");
        verify(audit).record(eq("super-1"), any(), eq("USER_ROLE_CHANGED"), eq("User"), eq("u1"), eq("ATHLETE -> ADMIN"));
    }

    @Test
    void sharedAdminEndpointsStillOpenToAdmin() {
        AuthenticatedPrincipal p = loginAs("admin-1", "ADMIN");
        when(userAdministration.suspend("u1", "admin-1", p.roles()))
                .thenReturn(new AdminUserDto("u1", "u1@x.com", "ATHLETE", List.of("ATHLETE"), "SUSPENDED", true, null, null));
        assertThat(admin.suspend("u1", p).getBody().status()).isEqualTo("SUSPENDED");
    }

    @Test
    void ordinaryUsersCannotReachAdminEndpoints() {
        AuthenticatedPrincipal p = loginAs("u1", "SCOUT");
        assertThatThrownBy(() -> admin.suspend("u2", p)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void superAdminCanCreateSport() {
        AuthenticatedPrincipal p = loginAs("super-1", "SUPER_ADMIN");
        when(sportService.createSport(any())).thenReturn(new SportDto("s1", "RG7", "Rugby 7s", null, true));
        assertThat(sports.create(new SportAdminDtos.CreateSportRequest("RG7", "Rugby 7s", null), p)
                .getStatusCode().value()).isEqualTo(201);
    }
}
