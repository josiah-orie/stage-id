package com.jovine360.talenthub.scout.infrastructure.web;

import com.jovine360.talenthub.scout.api.ScoutProfileDto;
import com.jovine360.talenthub.scout.application.service.ScoutProfileService;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ScoutController {

    private final ScoutProfileService scoutProfileService;

    public ScoutController(ScoutProfileService scoutProfileService) {
        this.scoutProfileService = scoutProfileService;
    }

    record CreateScoutRequest(String professionalTitle, String biography, Integer yearsExperience) {}

    @PostMapping("/scouts")
    public ResponseEntity<ScoutProfileDto> createProfile(
            @Valid @RequestBody CreateScoutRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        ScoutProfileDto dto = scoutProfileService.createScoutProfile(
                principal.userId(), req.professionalTitle(), req.biography(), req.yearsExperience());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/scouts/{id}")
    public ResponseEntity<ScoutProfileDto> getProfile(@PathVariable String id) {
        return scoutProfileService.getScoutProfile(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/scouts/verification-requests")
    public ResponseEntity<ScoutProfileDto> submitVerification(
            @RequestParam String scoutProfileId) {
        return ResponseEntity.ok(scoutProfileService.submitVerificationRequest(scoutProfileId));
    }

    @PostMapping("/admin/scouts/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ScoutProfileDto> approveVerification(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(scoutProfileService.approveVerification(id, principal.userId()));
    }

    @PostMapping("/admin/scouts/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ScoutProfileDto> rejectVerification(
            @PathVariable String id,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(scoutProfileService.rejectVerification(id, principal.userId(), notes));
    }
}
