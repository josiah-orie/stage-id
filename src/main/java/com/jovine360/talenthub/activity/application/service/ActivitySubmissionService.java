package com.jovine360.talenthub.activity.application.service;

import com.jovine360.talenthub.activity.domain.event.AthleteActivityPublishedEvent;
import com.jovine360.talenthub.activity.domain.event.AthleteActivitySubmittedEvent;
import com.jovine360.talenthub.activity.domain.model.*;
import com.jovine360.talenthub.activity.domain.repository.AthleteActivityFieldValueRepository;
import com.jovine360.talenthub.activity.domain.repository.AthleteActivityRepository;
import com.jovine360.talenthub.activityconfiguration.api.FieldDefinitionDto;
import com.jovine360.talenthub.activityconfiguration.api.PublishedSchemaDto;
import com.jovine360.talenthub.athleteaccess.api.AthleteAccessApi;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.athleteaccess.domain.model.GranteeType;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ActivitySubmissionService {

    private final AthleteActivityRepository activityRepository;
    private final AthleteActivityFieldValueRepository fieldValueRepository;
    private final ActivitySchemaValidator schemaValidator;
    private final AthleteAccessApi athleteAccessApi;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    public ActivitySubmissionService(
            AthleteActivityRepository activityRepository,
            AthleteActivityFieldValueRepository fieldValueRepository,
            ActivitySchemaValidator schemaValidator,
            AthleteAccessApi athleteAccessApi,
            ApplicationEventPublisher eventPublisher,
            MeterRegistry meterRegistry) {
        this.activityRepository = activityRepository;
        this.fieldValueRepository = fieldValueRepository;
        this.schemaValidator = schemaValidator;
        this.athleteAccessApi = athleteAccessApi;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;
    }

    public AthleteActivity createDraft(String athleteId, CreateActivityRequest request, String actorId) {
        athleteAccessApi.assertPermission(athleteId, actorId, GranteeType.USER, AthletePermission.MANAGE);

        PublishedSchemaDto schema = schemaValidator.validateAndGetSchema(
                request.templateId(), request.fieldValues());

        AthleteActivity activity = new AthleteActivity();
        activity.setAthleteId(athleteId);
        activity.setActivityTypeId(request.activityTypeId());
        activity.setSchemaVersionId(schema.schemaVersionId());
        activity.setSportId(request.sportId());
        activity.setTitle(request.title());
        activity.setDescription(request.description());
        activity.setActivityDate(request.activityDate() != null ? request.activityDate() : LocalDate.now());
        activity.setLocation(request.location());
        activity.setOrganizationId(request.organizationId());
        activity.setSubmittedBy(actorId);
        activity.setVisibilityLevel(request.visibilityLevel() != null
                ? request.visibilityLevel() : VisibilityLevel.PUBLIC);
        activity.setActivityStatus(ActivityStatus.DRAFT);
        activity.setVerificationStatus(VerificationStatus.UNVERIFIED);

        AthleteActivity saved = activityRepository.save(activity);
        saveFieldValues(saved.getId(), request.fieldValues(), schema.fields());
        return saved;
    }

    public AthleteActivity updateDraft(String activityId, CreateActivityRequest request, String actorId) {
        AthleteActivity activity = getAndAssertOwner(activityId, actorId);
        if (activity.getActivityStatus() != ActivityStatus.DRAFT) {
            throw new InvalidStateTransitionException("AthleteActivity",
                    activity.getActivityStatus().name(), "UPDATE");
        }
        if (request.title() != null) activity.setTitle(request.title());
        if (request.description() != null) activity.setDescription(request.description());
        if (request.activityDate() != null) activity.setActivityDate(request.activityDate());
        if (request.location() != null) activity.setLocation(request.location());
        if (request.visibilityLevel() != null) activity.setVisibilityLevel(request.visibilityLevel());

        if (request.fieldValues() != null && !request.fieldValues().isEmpty()) {
            PublishedSchemaDto schema = schemaValidator.validateAndGetSchema(
                    request.templateId(), request.fieldValues());
            fieldValueRepository.deleteAll(fieldValueRepository.findByActivityId(activityId));
            saveFieldValues(activityId, request.fieldValues(), schema.fields());
        }
        return activityRepository.save(activity);
    }

    public AthleteActivity submitActivity(String activityId, String actorId) {
        AthleteActivity activity = getAndAssertOwner(activityId, actorId);
        if (activity.getActivityStatus() != ActivityStatus.DRAFT) {
            throw new InvalidStateTransitionException("AthleteActivity",
                    activity.getActivityStatus().name(), "SUBMITTED");
        }
        activity.setActivityStatus(ActivityStatus.SUBMITTED);
        activity.setSubmittedAt(Instant.now());
        AthleteActivity saved = activityRepository.save(activity);
        meterRegistry.counter("activity.submissions.total").increment();
        eventPublisher.publishEvent(new AthleteActivitySubmittedEvent(
                saved.getId(), saved.getAthleteId(),
                saved.getActivityTypeId(), saved.getSportId(), saved.getActivityDate()));
        return saved;
    }

    public AthleteActivity publishActivity(String activityId, String actorId) {
        AthleteActivity activity = getAndAssertOwner(activityId, actorId);
        if (activity.getActivityStatus() != ActivityStatus.SUBMITTED) {
            throw new InvalidStateTransitionException("AthleteActivity",
                    activity.getActivityStatus().name(), "PUBLISHED");
        }
        activity.setActivityStatus(ActivityStatus.PUBLISHED);
        AthleteActivity saved = activityRepository.save(activity);
        eventPublisher.publishEvent(new AthleteActivityPublishedEvent(
                saved.getId(), saved.getAthleteId(), saved.getSportId(),
                saved.getActivityDate(), List.of()));
        return saved;
    }

    public AthleteActivity rejectActivity(String activityId, String actorId) {
        AthleteActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException("AthleteActivity", activityId));
        activity.setActivityStatus(ActivityStatus.REJECTED);
        return activityRepository.save(activity);
    }

    private AthleteActivity getAndAssertOwner(String activityId, String actorId) {
        AthleteActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException("AthleteActivity", activityId));
        athleteAccessApi.assertPermission(activity.getAthleteId(), actorId,
                GranteeType.USER, AthletePermission.MANAGE);
        return activity;
    }

    private void saveFieldValues(String activityId, Map<String, String> fieldValues,
                                  List<FieldDefinitionDto> fields) {
        if (fieldValues == null) return;
        for (FieldDefinitionDto field : fields) {
            String raw = fieldValues.get(field.fieldCode());
            if (raw == null) continue;
            AthleteActivityFieldValue fv = new AthleteActivityFieldValue();
            fv.setActivityId(activityId);
            fv.setFieldDefinitionId(field.id());
            fv.setFieldCode(field.fieldCode());
            fv.setStringValue(raw);
            fieldValueRepository.save(fv);
        }
    }

    /** DTO for creating/updating an activity. */
    public record CreateActivityRequest(
            String templateId,
            String activityTypeId,
            String sportId,
            String title,
            String description,
            LocalDate activityDate,
            String location,
            String organizationId,
            VisibilityLevel visibilityLevel,
            Map<String, String> fieldValues
    ) {}
}
