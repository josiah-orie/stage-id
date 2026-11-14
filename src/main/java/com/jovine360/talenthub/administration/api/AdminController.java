package com.jovine360.talenthub.administration.api;

import com.jovine360.talenthub.administration.infrastructure.persistence.PlatformMetricsQuery;
import com.jovine360.talenthub.identity.api.AdminUserDto;
import com.jovine360.talenthub.identity.api.UserAdministrationApi;
import com.jovine360.talenthub.scout.api.ScoutApi;
import com.jovine360.talenthub.scout.api.ScoutProfileDto;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.domain.service.AuditTrail;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Platform administration available to ADMIN and SUPER_ADMIN.
 * Actions on administrator accounts are further restricted to SUPER_ADMIN in the service.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "Administration", description = "Users, dashboard and review queues")
public class AdminController {

    private final UserAdministrationApi userAdministration;
    private final ScoutApi scoutApi;
    private final PlatformMetricsQuery metrics;
    private final AuditTrail audit;

    public AdminController(UserAdministrationApi userAdministration, ScoutApi scoutApi,
                           PlatformMetricsQuery metrics, AuditTrail audit) {
        this.userAdministration = userAdministration;
        this.scoutApi = scoutApi;
        this.metrics = metrics;
        this.audit = audit;
    }

    @Operation(summary = "Platform dashboard counts")
    @GetMapping("/reports/platform")
    public ResponseEntity<PlatformDashboardResponse> dashboard() {
        return ResponseEntity.ok(metrics.load());
    }

    @Operation(summary = "List users, optionally filtered by role and status")
    @GetMapping("/users")
    public ResponseEntity<PageResponse<AdminUserDto>> users(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(userAdministration.listUsers(role, status, pageable)));
    }

    @Operation(summary = "Get one user")
    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDto> user(@PathVariable String id) {
        return ResponseEntity.ok(userAdministration.getUser(id));
    }

    @Operation(summary = "Suspend a user. Administrator accounts need SUPER_ADMIN")
    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<AdminUserDto> suspend(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        AdminUserDto result = userAdministration.suspend(id, principal.userId(), principal.roles());
        audit.record(principal.userId(), principal.roles(), "USER_SUSPENDED", "User", id, null);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Reactivate a user. Administrator accounts need SUPER_ADMIN")
    @PostMapping("/users/{id}/reactivate")
    public ResponseEntity<AdminUserDto> reactivate(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        AdminUserDto result = userAdministration.reactivate(id, principal.userId(), principal.roles());
        audit.record(principal.userId(), principal.roles(), "USER_REACTIVATED", "User", id, null);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "List scout profiles by verification status (default PENDING)")
    @GetMapping("/scouts")
    public ResponseEntity<PageResponse<ScoutProfileDto>> scouts(
            @RequestParam(defaultValue = "PENDING") String status, Pageable pageable) {
        String filter = "ALL".equalsIgnoreCase(status) ? null : status;
        return ResponseEntity.ok(PageResponse.from(scoutApi.listByVerificationStatus(filter, pageable)));
    }
}
