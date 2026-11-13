package com.jovine360.talenthub.scouting.infrastructure.web;

import com.jovine360.talenthub.scouting.application.service.OrganizationScoutingWorkspaceService;
import com.jovine360.talenthub.scouting.domain.model.*;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/scouting")
public class OrgScoutingController {

    private final OrganizationScoutingWorkspaceService workspaceService;

    public OrgScoutingController(OrganizationScoutingWorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping("/workspace/activate")
    @PreAuthorize("hasAnyRole('ORG_ADMIN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> activateWorkspace(@PathVariable String orgId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        workspaceService.activateWorkspace(orgId, principal.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/seats")
    @PreAuthorize("hasAnyRole('ORG_ADMIN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<SubscriptionSeat> addSeat(@PathVariable String orgId,
            @RequestParam String subscriptionId, @RequestParam String userId) {
        return ResponseEntity.ok(workspaceService.addScoutSeat(subscriptionId, orgId, userId));
    }

    @GetMapping("/watchlists")
    @PreAuthorize("hasAnyRole('ORG_ADMIN','SCOUT','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<Watchlist>> getWatchlists(@PathVariable String orgId) {
        return ResponseEntity.ok(workspaceService.getSharedWatchlists(orgId));
    }

    @GetMapping("/evaluations")
    @PreAuthorize("hasAnyRole('ORG_ADMIN','SCOUT','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<ScoutEvaluation>> getEvaluations(@PathVariable String orgId) {
        return ResponseEntity.ok(workspaceService.getOrgEvaluations(orgId));
    }
}
