package com.jovine360.talenthub.athlete.api;

import com.jovine360.talenthub.athlete.application.service.AthleteService;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    public AthleteController(AthleteService athleteService) {
        this.athleteService = athleteService;
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
    public ResponseEntity<AthleteProfileDto> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(athleteService.getByUserId(userId));
    }

    @Operation(summary = "Get athlete profile by athlete number")
    @GetMapping("/number/{athleteNumber}")
    public ResponseEntity<AthleteProfileDto> getByAthleteNumber(@PathVariable String athleteNumber) {
        return ResponseEntity.ok(
                athleteService.findByAthleteNumber(athleteNumber)
                        .orElseThrow(() -> new com.jovine360.talenthub.shared.domain.exception
                                .EntityNotFoundException("AthleteProfile", athleteNumber)));
    }
}
