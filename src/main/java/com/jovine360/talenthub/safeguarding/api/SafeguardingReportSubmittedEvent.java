package com.jovine360.talenthub.safeguarding.api;

/** Published when anyone submits a safeguarding report. */
public record SafeguardingReportSubmittedEvent(
        String reportId, String reportedEntityId, String reportedEntityType, String reportType
) {}
