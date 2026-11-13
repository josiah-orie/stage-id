package com.jovine360.talenthub.activityconfiguration.domain.event;

/**
 * Published when an ActivitySchemaVersion moves to PUBLISHED status.
 * After this point the schema is immutable.
 */
public record ActivitySchemaPublishedEvent(
        String schemaVersionId,
        String templateId,
        int versionNumber
) {}
