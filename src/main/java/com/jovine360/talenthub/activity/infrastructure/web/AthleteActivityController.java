package com.jovine360.talenthub.activity.infrastructure.web;

import com.jovine360.talenthub.activity.application.service.ActivityQueryService;
import com.jovine360.talenthub.activity.application.service.ActivitySubmissionService;
import com.jovine360.talenthub.activity.application.service.ActivitySubmissionService.CreateActivityRequest;
import com.jovine360.talenthub.activity.domain.model.AthleteActivity;
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

    public AthleteActivityController(ActivitySubmissionService submissionService,
                                      ActivityQueryService queryService) {
        this.submissionService = submissionService;
        this.queryService = queryService;
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
            @PathVariable String id, Pageable pageable) {
        return ResponseEntity.ok(queryService.getAthleteActivities(id, pageable));
    }

    @GetMapping("/activities/{id}")
    public ResponseEntity<AthleteActivity> getActivity(@PathVariable String id) {
        return ResponseEntity.ok(queryService.getById(id));
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

    @GetMapping("/activities/{id}/measurements")
    public ResponseEntity<Void> getMeasurements(@PathVariable String id) {
        // Stub - full implementation in development module
        return ResponseEntity.ok().build();
    }
}
