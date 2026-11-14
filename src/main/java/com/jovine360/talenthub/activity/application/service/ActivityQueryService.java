package com.jovine360.talenthub.activity.application.service;

import com.jovine360.talenthub.activity.domain.model.ActivityStatus;
import com.jovine360.talenthub.activity.domain.model.AthleteActivity;
import com.jovine360.talenthub.activity.domain.model.VisibilityLevel;
import com.jovine360.talenthub.activity.domain.repository.AthleteActivityRepository;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ActivityQueryService implements com.jovine360.talenthub.activity.api.ActivityApi {

    private final AthleteActivityRepository activityRepository;

    public ActivityQueryService(AthleteActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public PageResponse<AthleteActivity> getAthleteActivities(String athleteId, Pageable pageable) {
        return getAthleteActivities(athleteId, pageable, false);
    }

    /** @param publicOnly when true, only PUBLISHED activities with PUBLIC visibility are returned */
    public PageResponse<AthleteActivity> getAthleteActivities(String athleteId, Pageable pageable, boolean publicOnly) {
        if (publicOnly) {
            return PageResponse.from(activityRepository.findByAthleteIdAndActivityStatusAndVisibilityLevel(
                    athleteId, ActivityStatus.PUBLISHED, VisibilityLevel.PUBLIC, pageable));
        }
        return PageResponse.from(activityRepository.findByAthleteId(athleteId, pageable));
    }

    public AthleteActivity getById(String activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException("AthleteActivity", activityId));
    }

    @Override
    public java.util.Optional<String> findAthleteId(String activityId) {
        return activityRepository.findById(activityId).map(AthleteActivity::getAthleteId);
    }

    @Override
    @Transactional
    public void markVerified(String activityId) {
        AthleteActivity activity = getById(activityId);
        activity.setVerificationStatus(com.jovine360.talenthub.activity.domain.model.VerificationStatus.VERIFIED);
        activityRepository.save(activity);
    }
}
