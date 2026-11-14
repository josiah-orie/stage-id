package com.jovine360.talenthub.verification.application.service;

import com.jovine360.talenthub.activity.api.ActivityApi;
import com.jovine360.talenthub.athleteaccess.api.AthleteAccessPolicy;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.verification.domain.event.AthleteActivityVerifiedEvent;
import com.jovine360.talenthub.verification.domain.model.ActivityVerification;
import com.jovine360.talenthub.verification.domain.repository.ActivityVerificationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;

@Service
@Transactional
public class VerificationService {

    private final ActivityApi activityApi;
    private final ActivityVerificationRepository verificationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AthleteAccessPolicy accessPolicy;

    public VerificationService(ActivityApi activityApi,
                               ActivityVerificationRepository verificationRepository,
                               ApplicationEventPublisher eventPublisher,
                               AthleteAccessPolicy accessPolicy) {
        this.activityApi = activityApi;
        this.verificationRepository = verificationRepository;
        this.eventPublisher = eventPublisher;
        this.accessPolicy = accessPolicy;
    }

    /**
     * A coach must have VIEW_STATS on the athlete (through an organization affiliation or a grant)
     * and cannot verify their own activity. Admins may verify any activity.
     */
    public void verifyActivity(String activityId, String verifierId, Collection<String> verifierRoles) {
        String athleteId = activityApi.findAthleteId(activityId)
                .orElseThrow(() -> new EntityNotFoundException("AthleteActivity", activityId));
        if (athleteId.equals(verifierId)) {
            throw new SecurityException("You cannot verify your own activity");
        }
        accessPolicy.assertCanAccess(athleteId, verifierId, verifierRoles, AthletePermission.VIEW_STATS);

        activityApi.markVerified(activityId);

        ActivityVerification verification = new ActivityVerification();
        verification.setActivityId(activityId);
        verification.setVerifierId(verifierId);
        verification.setVerifiedAt(Instant.now());
        verificationRepository.save(verification);

        eventPublisher.publishEvent(new AthleteActivityVerifiedEvent(activityId, athleteId, verifierId));
    }
}
