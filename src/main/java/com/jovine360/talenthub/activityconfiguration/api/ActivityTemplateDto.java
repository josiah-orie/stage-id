package com.jovine360.talenthub.activityconfiguration.api;

public record ActivityTemplateDto(
        String id, String code, String name, String description,
        String schemaJson, String sportCode, boolean active
) {}
