package com.jovine360.talenthub.scout.application.service;

import com.jovine360.talenthub.scout.api.ScoutApi;
import com.jovine360.talenthub.scout.api.ScoutProfileDto;
import com.jovine360.talenthub.scout.domain.model.*;
import com.jovine360.talenthub.scout.domain.repository.ScoutProfileRepository;
import com.jovine360.talenthub.scout.domain.repository.ScoutVerificationRepository;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.service.EntitlementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
public class ScoutProfileService implements ScoutApi {

    private final ScoutProfileRepository profileRepository;
    private final ScoutVerificationRepository verificationRepository;
    private final EntitlementService entitlementService;

    public ScoutProfileService(ScoutProfileRepository profileRepository,
                                ScoutVerificationRepository verificationRepository,
                                EntitlementService entitlementService) {
        this.profileRepository = profileRepository;
        this.verificationRepository = verificationRepository;
        this.entitlementService = entitlementService;
    }

    public ScoutProfileDto createScoutProfile(String userAccountId, String professionalTitle,
                                               String biography, Integer yearsExperience) {
        if (profileRepository.existsByUserAccountId(userAccountId)) {
            throw new DuplicateResourceException("ScoutProfile", "userAccountId", userAccountId);
        }
        ScoutProfile profile = new ScoutProfile();
        profile.setUserAccountId(userAccountId);
        profile.setProfessionalTitle(professionalTitle);
        profile.setBiography(biography);
        profile.setYearsExperience(yearsExperience);
        profile.setVerificationStatus(ScoutVerificationStatus.UNVERIFIED);
        return toDto(profileRepository.save(profile));
    }

    public ScoutProfileDto updateScoutProfile(String scoutProfileId, String professionalTitle,
                                               String biography, Integer yearsExperience) {
        ScoutProfile profile = profileRepository.findById(scoutProfileId)
                .orElseThrow(() -> new EntityNotFoundException("ScoutProfile", scoutProfileId));
        if (professionalTitle != null) profile.setProfessionalTitle(professionalTitle);
        if (biography != null) profile.setBiography(biography);
        if (yearsExperience != null) profile.setYearsExperience(yearsExperience);
        return toDto(profileRepository.save(profile));
    }

    public ScoutProfileDto submitVerificationRequest(String scoutProfileId) {
        ScoutProfile profile = profileRepository.findById(scoutProfileId)
                .orElseThrow(() -> new EntityNotFoundException("ScoutProfile", scoutProfileId));
        ScoutVerification verification = new ScoutVerification();
        verification.setScoutProfileId(scoutProfileId);
        verification.setSubmittedAt(Instant.now());
        verification.setStatus(ScoutVerificationStatus.PENDING);
        verificationRepository.save(verification);
        profile.setVerificationStatus(ScoutVerificationStatus.PENDING);
        return toDto(profileRepository.save(profile));
    }

    public ScoutProfileDto approveVerification(String scoutProfileId, String reviewedBy) {
        ScoutProfile profile = profileRepository.findById(scoutProfileId)
                .orElseThrow(() -> new EntityNotFoundException("ScoutProfile", scoutProfileId));
        profile.setVerificationStatus(ScoutVerificationStatus.VERIFIED);
        verificationRepository.findByScoutProfileId(scoutProfileId).stream()
                .filter(v -> v.getStatus() == ScoutVerificationStatus.PENDING)
                .forEach(v -> {
                    v.setStatus(ScoutVerificationStatus.VERIFIED);
                    v.setReviewedBy(reviewedBy);
                    v.setReviewedAt(Instant.now());
                    verificationRepository.save(v);
                });
        return toDto(profileRepository.save(profile));
    }

    public ScoutProfileDto rejectVerification(String scoutProfileId, String reviewedBy, String notes) {
        ScoutProfile profile = profileRepository.findById(scoutProfileId)
                .orElseThrow(() -> new EntityNotFoundException("ScoutProfile", scoutProfileId));
        profile.setVerificationStatus(ScoutVerificationStatus.REJECTED);
        verificationRepository.findByScoutProfileId(scoutProfileId).stream()
                .filter(v -> v.getStatus() == ScoutVerificationStatus.PENDING)
                .forEach(v -> {
                    v.setStatus(ScoutVerificationStatus.REJECTED);
                    v.setReviewedBy(reviewedBy);
                    v.setReviewedAt(Instant.now());
                    v.setNotes(notes);
                    verificationRepository.save(v);
                });
        return toDto(profileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScoutProfileDto> getScoutProfile(String scoutProfileId) {
        return profileRepository.findById(scoutProfileId).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ScoutProfileDto> listByVerificationStatus(
            String status, org.springframework.data.domain.Pageable pageable) {
        if (status == null || status.isBlank()) {
            return profileRepository.findAll(pageable).map(this::toDto);
        }
        return profileRepository.findByVerificationStatus(
                ScoutVerificationStatus.valueOf(status.toUpperCase()), pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findScoutProfileIdByUserId(String userAccountId) {
        return profileRepository.findByUserAccountId(userAccountId).map(p -> p.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public String requireScoutProfileIdForUser(String userAccountId) {
        return findScoutProfileIdByUserId(userAccountId)
                .orElseThrow(() -> new com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException(
                        "No scout profile exists for this account. Create one with POST /scouts first."));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isVerifiedScout(String userAccountId) {
        return profileRepository.findByUserAccountId(userAccountId)
                .map(p -> p.getVerificationStatus() == ScoutVerificationStatus.VERIFIED)
                .orElse(false);
    }

    private ScoutProfileDto toDto(ScoutProfile p) {
        return new ScoutProfileDto(p.getId(), p.getUserAccountId(), p.getProfessionalTitle(),
                p.getBiography(), p.getYearsExperience(), p.getCurrentOrganizationId(),
                p.getVerificationStatus().name());
    }
}
