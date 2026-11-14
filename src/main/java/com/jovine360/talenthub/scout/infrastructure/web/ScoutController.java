package com.jovine360.talenthub.scout.infrastructure.web;

import com.jovine360.talenthub.scout.api.ScoutProfileDto;
import com.jovine360.talenthub.scout.application.service.ScoutProfileService;
import com.jovine360.talenthub.shared.domain.service.AuditTrail;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ScoutController {

    private final ScoutProfileService scoutProfileService;

    private final AuditTrail audit;
    private final com.jovine360.talenthub.scout.application.service.ScoutSpecializationService specializations;

    public ScoutController(ScoutProfileService scoutProfileService, AuditTrail audit,
                           com.jovine360.talenthub.scout.application.service.ScoutSpecializationService specializations) {
        this.scoutProfileService = scoutProfileService;
        this.audit = audit;
        this.specializations = specializations;
    }

    record SpecializationRequest(@jakarta.validation.constraints.NotBlank String sportCode,
                                 com.jovine360.talenthub.scout.domain.model.ScoutingLevel level,
                                 List<String> preferredRegions) {}

    @PostMapping("/scouts/me/specializations")
    public ResponseEntity<com.jovine360.talenthub.scout.api.SpecializationDto> addSpecialization(
            @Valid @RequestBody SpecializationRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        String scoutProfileId = scoutProfileService.requireScoutProfileIdForUser(principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(specializations.add(scoutProfileId, req.sportCode(), req.level(), req.preferredRegions()));
    }

    @GetMapping("/scouts/me/specializations")
    public ResponseEntity<List<com.jovine360.talenthub.scout.api.SpecializationDto>> mySpecializations(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(specializations.list(scoutProfileService.requireScoutProfileIdForUser(principal.userId())));
    }

    @DeleteMapping("/scouts/me/specializations/{id}")
    public ResponseEntity<Void> removeSpecialization(@PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        specializations.remove(scoutProfileService.requireScoutProfileIdForUser(principal.userId()), id);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/scouts/me")
    public ResponseEntity<ScoutProfileDto> getMyProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        String scoutProfileId = scoutProfileService.requireScoutProfileIdForUser(principal.userId());
        return ResponseEntity.ok(scoutProfileService.getScoutProfile(scoutProfileId).orElseThrow());
    }

    @GetMapping("/scouts/{id}")
    public ResponseEntity<ScoutProfileDto> getProfile(@PathVariable String id) {
        return scoutProfileService.getScoutProfile(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/scouts/verification-requests")
    public ResponseEntity<ScoutProfileDto> submitVerification(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        String scoutProfileId = scoutProfileService.requireScoutProfileIdForUser(principal.userId());
        return ResponseEntity.ok(scoutProfileService.submitVerificationRequest(scoutProfileId));
    }

    @PostMapping("/admin/scouts/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ScoutProfileDto> approveVerification(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        ScoutProfileDto result = scoutProfileService.approveVerification(id, principal.userId());
        audit.record(principal.userId(), principal.roles(), "SCOUT_VERIFIED", "ScoutProfile", id, null);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/admin/scouts/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ScoutProfileDto> rejectVerification(
            @PathVariable String id,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        ScoutProfileDto result = scoutProfileService.rejectVerification(id, principal.userId(), notes);
        audit.record(principal.userId(), principal.roles(), "SCOUT_REJECTED", "ScoutProfile", id, notes);
        return ResponseEntity.ok(result);
    }
}
