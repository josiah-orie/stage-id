package com.jovine360.talenthub.activityconfiguration.application.service;

/**
 * @deprecated Use {@link ActivityConfigurationService} directly.
 * Kept as a type alias so existing code that autowires this class still compiles.
 */
@Deprecated(since = "phase2")
public class ActivityConfigService extends ActivityConfigurationService {

    public ActivityConfigService(
            com.jovine360.talenthub.activityconfiguration.domain.repository.ActivityTemplateRepository templateRepository,
            com.jovine360.talenthub.activityconfiguration.domain.repository.ActivityTypeRepository typeRepository,
            com.jovine360.talenthub.activityconfiguration.domain.repository.ActivitySchemaVersionRepository schemaVersionRepository,
            com.jovine360.talenthub.activityconfiguration.domain.repository.ActivityFieldDefinitionRepository fieldDefinitionRepository,
            com.jovine360.talenthub.activityconfiguration.domain.repository.ActivityFieldOptionRepository fieldOptionRepository,
            org.springframework.context.ApplicationEventPublisher eventPublisher) {
        super(templateRepository, typeRepository, schemaVersionRepository,
              fieldDefinitionRepository, fieldOptionRepository, eventPublisher);
    }
}
