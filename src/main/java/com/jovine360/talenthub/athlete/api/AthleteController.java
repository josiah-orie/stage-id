package com.jovine360.talenthub.athlete.api;

import com.jovine360.talenthub.athlete.application.service.AthleteService;
import com.jovine360.talenthub.athleteaccess.api.AthleteAccessPolicy;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/athletes")
@Tag(name = "Athletes", description = "Athlete profile management")
public class AthleteController {

    private final AthleteService athleteService;
    private final AthleteAccessPolicy accessPolicy;

    public AthleteController(AthleteService athleteService, AthleteAccessPolicy accessPolicy) {
        this.athleteService = athleteService;
        this.accessPolicy = accessPolicy;
    }

    @Operation(summary = "Create my athlete profile")
    @PostMapping("/me")
    public ResponseEntity<AthleteProfileDto> createProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateAthleteProfileRequest request) {
        AthleteProfileDto dto = athleteService.createAthleteProfile(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Get my athlete profile")
    @GetMapping("/me")
    public ResponseEntity<AthleteProfileDto> getMyProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(athleteService.getByUserId(principal.userId()));
    }

    @Operation(summary = "Update my athlete profile")
    @PatchMapping("/me")
    public ResponseEntity<AthleteProfileDto> updateProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateAthleteProfileRequest request) {
        return ResponseEntity.ok(athleteService.updateAthleteProfile(principal.userId(), request));
    }

    @Operation(summary = "Get athlete profile by user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<AthleteProfileDto> getByUserId(
            @PathVariable String userId, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        accessPolicy.assertCanAccess(userId, principal.userId(), principal.roles(), AthletePermission.VIEW_PROFILE);
        return ResponseEntity.ok(athleteService.getByUserId(userId));
    }

    @Operation(summary = "Get an athlete profile by the athlete's user id (needs VIEW_PROFILE)")
    @GetMapping("/{id}")
    public ResponseEntity<AthleteProfileDto> getById(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        accessPolicy.assertCanAccess(id, principal.userId(), principal.roles(), AthletePermission.VIEW_PROFILE);
        return ResponseEntity.ok(athleteService.getByUserId(id));
    }

    @Operation(summary = "Update an athlete profile by the athlete's user id (needs MANAGE)")
    @PatchMapping("/{id}")
    public ResponseEntity<AthleteProfileDto> updateById(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateAthleteProfileRequest request) {
        accessPolicy.assertCanAccess(id, principal.userId(), principal.roles(), AthletePermission.MANAGE);
        return ResponseEntity.ok(athleteService.updateAthleteProfile(id, request));
    }

    @Operation(summary = "List the athlete's sports, primary first (needs VIEW_PROFILE)")
    @GetMapping("/{id}/sports")
    public ResponseEntity<java.util.List<AthleteSportDto>> sports(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        accessPolicy.assertCanAccess(id, principal.userId(), principal.roles(), AthletePermission.VIEW_PROFILE);
        return ResponseEntity.ok(athleteService.listSports(id));
    }

    @Operation(summary = "Add a secondary sport (needs MANAGE)")
    @PostMapping("/{id}/sports")
    public ResponseEntity<AthleteSportDto> addSport(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody AddAthleteSportRequest request) {
        accessPolicy.assertCanAccess(id, principal.userId(), principal.roles(), AthletePermission.MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED).body(athleteService.addSport(id, request));
    }

    @Operation(summary = "Remove a secondary sport (needs MANAGE)")
    @DeleteMapping("/{id}/sports/{athleteSportId}")
    public ResponseEntity<Void> removeSport(
            @PathVariable String id, @PathVariable String athleteSportId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        accessPolicy.assertCanAccess(id, principal.userId(), principal.roles(), AthletePermission.MANAGE);
        athleteService.removeSport(id, athleteSportId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get athlete profile by athlete number")
    @GetMapping("/number/{athleteNumber}")
    public ResponseEntity<AthleteProfileDto> getByAthleteNumber(
            @PathVariable String athleteNumber, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        AthleteProfileDto profile = athleteService.findByAthleteNumber(athleteNumber)
                .orElseThrow(() -> new com.jovine360.talenthub.shared.domain.exception
                        .EntityNotFoundException("AthleteProfile", athleteNumber));
        if (accessPolicy.canAccess(profile.userId(), principal.userId(), principal.roles(),
                AthletePermission.VIEW_PROFILE)) {
            return ResponseEntity.ok(profile);
        }
        // Everyone else gets the public projection: enough to identify the athlete, nothing more
        return ResponseEntity.ok(new AthleteProfileDto(null, profile.userId(), profile.athleteNumber(),
                null, profile.primarySportCode(), null, null, null, null, profile.status(), null, null));
    }
}
