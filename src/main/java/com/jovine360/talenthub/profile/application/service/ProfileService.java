package com.jovine360.talenthub.profile.application.service;

import com.jovine360.talenthub.identity.domain.model.UserRole;
import com.jovine360.talenthub.profile.api.UpdateProfileRequest;
import com.jovine360.talenthub.profile.api.UserProfileApi;
import com.jovine360.talenthub.profile.api.UserProfileDto;
import com.jovine360.talenthub.profile.domain.model.UserProfile;
import com.jovine360.talenthub.profile.domain.repository.UserProfileRepository;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Profile application service — implements {@link UserProfileApi} for cross-module use.
 */
@Service
@Transactional
public class ProfileService implements UserProfileApi {

    private final UserProfileRepository profileRepository;

    public ProfileService(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * Creates an initial empty profile for a newly registered user.
     */
    public void createProfile(String userId, String email, UserRole role) {
        if (profileRepository.existsByUserId(userId)) {
            return; // idempotent
        }
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setRole(role);
        // Derive a display name from the email as a starter
        profile.setDisplayName(email.split("@")[0]);
        profileRepository.save(profile);
    }

    /**
     * Updates profile fields from the request. Only non-null fields are applied.
     */
    public UserProfileDto updateProfile(String userId, UpdateProfileRequest req) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserProfile", userId));

        if (req.firstName()    != null) profile.setFirstName(req.firstName());
        if (req.lastName()     != null) profile.setLastName(req.lastName());
        if (req.displayName()  != null) profile.setDisplayName(req.displayName());
        if (req.dateOfBirth()  != null) profile.setDateOfBirth(req.dateOfBirth());
        if (req.gender()       != null) profile.setGender(req.gender());
        if (req.phoneNumber()  != null) profile.setPhoneNumber(req.phoneNumber());
        if (req.countryCode()  != null) profile.setCountryCode(req.countryCode());
        if (req.city()         != null) profile.setCity(req.city());
        if (req.bio()          != null) profile.setBio(req.bio());

        profile.refreshCompleteness();
        return toDto(profileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getProfileByUserId(String userId) {
        return profileRepository.findByUserId(userId)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProfileComplete(String userId) {
        return profileRepository.findByUserId(userId)
                .map(UserProfile::isProfileComplete)
                .orElse(false);
    }

    private UserProfileDto toDto(UserProfile p) {
        return new UserProfileDto(
                p.getId(), p.getUserId(), p.getFirstName(), p.getLastName(),
                p.getDisplayName(), p.getDateOfBirth(), p.getGender(),
                p.getPhoneNumber(), p.getCountryCode(), p.getCity(),
                p.getBio(), p.getAvatarUrl(),
                p.getRole() != null ? p.getRole().name() : null,
                p.isProfileComplete()
        );
    }
}
