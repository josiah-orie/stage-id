package com.jovine360.talenthub.scouting.infrastructure.web;

import com.jovine360.talenthub.organization.api.OrganizationApi;
import com.jovine360.talenthub.scouting.application.service.OrganizationScoutingWorkspaceService;
import com.jovine360.talenthub.scouting.domain.model.*;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Organization scouting workspace. The platform role is checked first, then membership of
 * this particular organization: an ORG_ADMIN of one organization cannot manage another.
 */
@RestController
@RequestMapping("/api/v1/organizations/{orgId}/scouting")
public class OrgScoutingController {

    private final OrganizationScoutingWorkspaceService workspaceService;
    private final OrganizationApi organizationApi;

    public OrgScoutingController(OrganizationScoutingWorkspaceService workspaceService,
                                 OrganizationApi organizationApi) {
        this.workspaceService = workspaceService;
        this.organizationApi = organizationApi;
    }

    private static boolean isPlatformAdmin(AuthenticatedPrincipal p) {
        return p.roles().contains("ADMIN") || p.roles().contains("SUPER_ADMIN");
    }

    private void assertOrgAdmin(String orgId, AuthenticatedPrincipal p) {
        if (!isPlatformAdmin(p) && !organizationApi.isOrganizationAdmin(orgId, p.userId())) {
            throw new SecurityException("Only this organization's OWNER or ADMIN can do this");
        }
    }

    private void assertMember(String orgId, AuthenticatedPrincipal p) {
        if (!isPlatformAdmin(p) && !organizationApi.isMember(orgId, p.userId())) {
            throw new SecurityException("Only members of this organization can see this");
        }
    }

    @PostMapping("/workspace/activate")
    @PreAuthorize("hasAnyRole('ORG_ADMIN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> activateWorkspace(@PathVariable String orgId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        assertOrgAdmin(orgId, principal);
        workspaceService.activateWorkspace(orgId, principal.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/seats")
    @PreAuthorize("hasAnyRole('ORG_ADMIN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<SubscriptionSeat> addSeat(@PathVariable String orgId,
            @RequestParam String subscriptionId, @RequestParam String userId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        assertOrgAdmin(orgId, principal);
        return ResponseEntity.ok(workspaceService.addScoutSeat(subscriptionId, orgId, userId));
    }

    @GetMapping("/watchlists")
    @PreAuthorize("hasAnyRole('ORG_ADMIN','SCOUT','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<Watchlist>> getWatchlists(@PathVariable String orgId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        assertMember(orgId, principal);
        return ResponseEntity.ok(workspaceService.getSharedWatchlists(orgId));
    }

    @GetMapping("/evaluations")
    @PreAuthorize("hasAnyRole('ORG_ADMIN','SCOUT','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<ScoutEvaluation>> getEvaluations(@PathVariable String orgId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        assertMember(orgId, principal);
        return ResponseEntity.ok(workspaceService.getOrgEvaluations(orgId));
    }
}
