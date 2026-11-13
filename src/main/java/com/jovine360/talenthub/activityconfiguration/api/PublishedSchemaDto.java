package com.jovine360.talenthub.activityconfiguration.api;

import java.util.List;

public record PublishedSchemaDto(
        String schemaVersionId,
        String templateId,
        int versionNumber,
        List<FieldDefinitionDto> fields
) {}
