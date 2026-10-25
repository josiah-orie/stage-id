package com.jovine360.talenthub.guardian.domain.service;

import com.jovine360.talenthub.guardian.domain.model.GuardianLinkStatus;
import com.jovine360.talenthub.guardian.domain.repository.GuardianLinkRepository;
import com.jovine360.talenthub.profile.api.UserProfileApi;
import com.jovine360.talenthub.shared.domain.exception.SafeguardingViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

/**
 * Enforces safeguarding policies for minor athletes (under 18).
 */
@Service
@Transactional(readOnly = true)
public class SafeguardingPolicyService {

    private final GuardianLinkRepository guardianLinkRepository;
    private final UserProfileApi userProfileApi;

    public SafeguardingPolicyService(GuardianLinkRepository guardianLinkRepository,
                                     UserProfileApi userProfileApi) {
        this.guardianLinkRepository = guardianLinkRepository;
        this.userProfileApi = userProfileApi;
    }

    /**
     * Returns true if the athlete is a minor (under 18).
     */
    public boolean isMinor(String athleteUserId) {
        var profile = userProfileApi.getProfileByUserId(athleteUserId);
        if (profile == null || profile.dateOfBirth() == null) return false;
        return Period.between(profile.dateOfBirth(), LocalDate.now()).getYears() < 18;
    }

    /**
     * Asserts that a minor athlete has an active guardian link with consent.
     * Throws {@link SafeguardingViolationException} if the check fails.
     */
    public void assertGuardianConsent(String athleteUserId) {
        if (!isMinor(athleteUserId)) return;

        boolean hasConsent = guardianLinkRepository
                .findByAthleteUserIdAndStatus(athleteUserId, GuardianLinkStatus.ACTIVE)
                .stream()
                .anyMatch(link -> link.isConsentGiven());

        if (!hasConsent) {
            throw new SafeguardingViolationException("GUARDIAN_CONSENT_REQUIRED",
                    "Athlete is a minor and requires active guardian consent to proceed.");
        }
    }

    /**
     * Checks that the guardian has an active link to the athlete.
     */
    public boolean isGuardianOf(String guardianUserId, String athleteUserId) {
        return guardianLinkRepository.existsByGuardianUserIdAndAthleteUserIdAndStatus(
                guardianUserId, athleteUserId, GuardianLinkStatus.ACTIVE);
    }
}
