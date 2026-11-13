package com.jovine360.talenthub.verification.application.service;

import com.jovine360.talenthub.activity.domain.model.AthleteActivity;
import com.jovine360.talenthub.activity.domain.model.VerificationStatus;
import com.jovine360.talenthub.activity.domain.repository.AthleteActivityRepository;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.verification.domain.event.AthleteActivityVerifiedEvent;
import com.jovine360.talenthub.verification.domain.model.ActivityVerification;
import com.jovine360.talenthub.verification.domain.repository.ActivityVerificationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class VerificationService {

    private final AthleteActivityRepository activityRepository;
    private final ActivityVerificationRepository verificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VerificationService(AthleteActivityRepository activityRepository,
                                ActivityVerificationRepository verificationRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.activityRepository = activityRepository;
        this.verificationRepository = verificationRepository;
        this.eventPublisher = eventPublisher;
    }

    public void verifyActivity(String activityId, String verifierId) {
        AthleteActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException("AthleteActivity", activityId));

        activity.setVerificationStatus(VerificationStatus.VERIFIED);
        activityRepository.save(activity);

        ActivityVerification verification = new ActivityVerification();
        verification.setActivityId(activityId);
        verification.setVerifierId(verifierId);
        verification.setVerifiedAt(Instant.now());
        verificationRepository.save(verification);

        eventPublisher.publishEvent(
                new AthleteActivityVerifiedEvent(activityId, activity.getAthleteId(), verifierId));
    }
}
