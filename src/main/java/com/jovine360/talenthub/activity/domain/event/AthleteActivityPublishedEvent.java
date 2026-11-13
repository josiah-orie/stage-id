package com.jovine360.talenthub.activity.domain.event;

import java.time.LocalDate;
import java.util.List;

public record AthleteActivityPublishedEvent(
        String activityId,
        String athleteId,
        String sportId,
        LocalDate activityDate,
        List<PerformanceSnapshot> performances
) {
    public record PerformanceSnapshot(
            String metricCode,
            java.math.BigDecimal measuredValue,
            String unit,
            String source
    ) {}
}
