package com.jovine360.talenthub.activity.infrastructure.web;

import com.jovine360.talenthub.activity.application.service.ActivityQueryService;
import com.jovine360.talenthub.activity.application.service.ActivitySubmissionService;
import com.jovine360.talenthub.activity.application.service.ActivitySubmissionService.CreateActivityRequest;
import com.jovine360.talenthub.activity.domain.model.ActivityStatus;
import com.jovine360.talenthub.activity.domain.model.AthleteActivity;
import com.jovine360.talenthub.activity.domain.model.VisibilityLevel;
import com.jovine360.talenthub.athleteaccess.api.AthleteAccessPolicy;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AthleteActivityController {

    private final ActivitySubmissionService submissionService;
    private final ActivityQueryService queryService;
    private final AthleteAccessPolicy accessPolicy;

    public AthleteActivityController(ActivitySubmissionService submissionService,
                                      ActivityQueryService queryService,
                                      AthleteAccessPolicy accessPolicy) {
        this.submissionService = submissionService;
        this.queryService = queryService;
        this.accessPolicy = accessPolicy;
    }

    /**
     * Related callers (athlete, guardian, grant holder, admin) see everything. A SCOUT sees only
     * published, public activities. Anyone else gets 403.
     */
    private boolean canSeeAll(String athleteId, AuthenticatedPrincipal principal) {
        if (accessPolicy.canAccess(athleteId, principal.userId(), principal.roles(), AthletePermission.VIEW_STATS)) {
            return true;
        }
        if (principal.roles().contains("SCOUT")) {
            return false;
        }
        accessPolicy.assertCanAccess(athleteId, principal.userId(), principal.roles(), AthletePermission.VIEW_STATS);
        return true;
    }

    private static boolean isPublicPublished(AthleteActivity a) {
        return a.getActivityStatus() == ActivityStatus.PUBLISHED && a.getVisibilityLevel() == VisibilityLevel.PUBLIC;
    }

    @PostMapping("/athletes/{id}/activities")
    public ResponseEntity<AthleteActivity> createDraft(
            @PathVariable String id,
            @Valid @RequestBody CreateActivityRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        AthleteActivity activity = submissionService.createDraft(id, request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(activity);
    }

    @GetMapping("/athletes/{id}/activities")
    public ResponseEntity<PageResponse<AthleteActivity>> listActivities(
            @PathVariable String id, Pageable pageable,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        boolean all = canSeeAll(id, principal);
        return ResponseEntity.ok(queryService.getAthleteActivities(id, pageable, !all));
    }

    @GetMapping("/activities/{id}")
    public ResponseEntity<AthleteActivity> getActivity(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        AthleteActivity activity = queryService.getById(id);
        boolean all = canSeeAll(activity.getAthleteId(), principal);
        if (!all && !isPublicPublished(activity)) {
            throw new com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException("AthleteActivity", id);
        }
        return ResponseEntity.ok(activity);
    }

    @PatchMapping("/activities/{id}")
    public ResponseEntity<AthleteActivity> updateActivity(
            @PathVariable String id,
            @RequestBody CreateActivityRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(submissionService.updateDraft(id, request, principal.userId()));
    }

    @PostMapping("/activities/{id}/submit")
    public ResponseEntity<AthleteActivity> submit(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(submissionService.submitActivity(id, principal.userId()));
    }

    @PostMapping("/activities/{id}/publish")
    public ResponseEntity<AthleteActivity> publish(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(submissionService.publishActivity(id, principal.userId()));
    }
}
