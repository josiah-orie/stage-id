package com.jovine360.talenthub.development.api;

import java.util.List;

public record DevelopmentTimelineResponse(
        String athleteId,
        List<TimelineEntry> entries
) {
    public record TimelineEntry(
            String date,
            String type,    // ACTIVITY | MEASUREMENT | MILESTONE
            String title,
            String detail,
            String entityId
    ) {}
}
