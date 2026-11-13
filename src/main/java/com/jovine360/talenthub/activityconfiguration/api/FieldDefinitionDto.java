package com.jovine360.talenthub.activityconfiguration.api;

public record FieldDefinitionDto(
        String id,
        String fieldCode,
        String fieldLabel,
        String fieldType,
        boolean required,
        int displayOrder,
        String validationRulesJson,
        String helpText
) {}
