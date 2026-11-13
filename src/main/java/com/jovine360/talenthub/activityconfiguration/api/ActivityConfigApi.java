package com.jovine360.talenthub.activityconfiguration.api;

import java.util.List;
import java.util.Optional;

/**
 * Public interface for the activity configuration module.
 * Other modules consume this to look up published schemas and field definitions.
 */
public interface ActivityConfigApi {
    List<ActivityTemplateDto> listActiveTemplates();
    Optional<ActivityTemplateDto> findByCode(String code);
    Optional<PublishedSchemaDto> getPublishedSchema(String templateId);
    Optional<FieldDefinitionDto> getFieldDefinition(String fieldId);
}
