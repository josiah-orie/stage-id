package com.jovine360.talenthub.activity.application.service;

import com.jovine360.talenthub.activity.domain.model.AthleteActivity;
import com.jovine360.talenthub.activity.domain.repository.AthleteActivityRepository;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ActivityQueryService {

    private final AthleteActivityRepository activityRepository;

    public ActivityQueryService(AthleteActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public PageResponse<AthleteActivity> getAthleteActivities(String athleteId, Pageable pageable) {
        return PageResponse.from(activityRepository.findByAthleteId(athleteId, pageable));
    }

    public AthleteActivity getById(String activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException("AthleteActivity", activityId));
    }
}
