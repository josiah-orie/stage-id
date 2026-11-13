package com.jovine360.talenthub.activity.application.service;

import com.jovine360.talenthub.activity.api.ActivitySessionDto;
import com.jovine360.talenthub.activity.api.LogActivityRequest;
import com.jovine360.talenthub.activity.domain.model.ActivitySession;
import com.jovine360.talenthub.activity.domain.repository.ActivitySessionRepository;
import com.jovine360.talenthub.activityconfiguration.api.ActivityConfigApi;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActivityService {

    private final ActivitySessionRepository sessionRepository;
    private final ActivityConfigApi activityConfigApi;

    public ActivityService(ActivitySessionRepository sessionRepository,
                           ActivityConfigApi activityConfigApi) {
        this.sessionRepository = sessionRepository;
        this.activityConfigApi = activityConfigApi;
    }

    public ActivitySessionDto logActivity(String athleteUserId, String loggedBy, LogActivityRequest req) {
        // Validate template exists
        if (activityConfigApi.findByCode(req.templateCode()).isEmpty()) {
            throw new EntityNotFoundException("ActivityTemplate", req.templateCode());
        }

        ActivitySession session = new ActivitySession();
        session.setAthleteUserId(athleteUserId);
        session.setTemplateCode(req.templateCode());
        session.setSessionDate(req.sessionDate());
        session.setSportCode(req.sportCode());
        session.setDataJson(req.dataJson());
        session.setCoachNotes(req.coachNotes());
        session.setLoggedBy(loggedBy);

        return toDto(sessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivitySessionDto> getAthleteActivities(String athleteUserId, Pageable pageable) {
        return PageResponse.from(sessionRepository.findByAthleteUserId(athleteUserId, pageable).map(this::toDto));
    }

    @Transactional(readOnly = true)
    public ActivitySessionDto getById(String id) {
        return sessionRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("ActivitySession", id));
    }

    private ActivitySessionDto toDto(ActivitySession s) {
        return new ActivitySessionDto(s.getId(), s.getAthleteUserId(), s.getTemplateCode(),
                s.getSessionDate(), s.getSportCode(), s.getDataJson(), s.getCoachNotes(), s.getLoggedBy());
    }
}
