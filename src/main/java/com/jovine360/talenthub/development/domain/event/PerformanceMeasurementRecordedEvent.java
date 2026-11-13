package com.jovine360.talenthub.development.domain.event;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PerformanceMeasurementRecordedEvent(
        String measurementId,
        String athleteId,
        String metricCode,
        BigDecimal measuredValue,
        String unit,
        LocalDate observedAt,
        String source
) {}
