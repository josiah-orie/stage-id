package com.jovine360.talenthub.profile.api;

import com.jovine360.talenthub.profile.application.service.ProfileService;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Profile REST endpoints.
 */
@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Profiles", description = "User profile management")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(summary = "Get my profile")
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        UserProfileDto dto = profileService.getProfileByUserId(principal.userId());
        if (dto == null) throw new EntityNotFoundException("UserProfile", principal.userId());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Update my profile")
    @PatchMapping("/me")
    public ResponseEntity<UserProfileDto> updateMyProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(principal.userId(), request));
    }

    @Operation(summary = "Get a user's profile. Other users get the public fields only")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable String userId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        UserProfileDto dto = profileService.getProfileByUserId(userId);
        if (dto == null) throw new EntityNotFoundException("UserProfile", userId);
        boolean full = userId.equals(principal.userId())
                || principal.roles().contains("ADMIN") || principal.roles().contains("SUPER_ADMIN");
        if (full) return ResponseEntity.ok(dto);
        // Personal details (legal name, date of birth, gender, phone) are never shown to other users
        return ResponseEntity.ok(new UserProfileDto(dto.id(), dto.userId(), null, null, dto.displayName(),
                null, null, null, dto.countryCode(), dto.city(), dto.bio(), dto.avatarUrl(), dto.role(),
                dto.profileComplete()));
    }
}
