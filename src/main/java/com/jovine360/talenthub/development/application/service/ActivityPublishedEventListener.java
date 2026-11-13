package com.jovine360.talenthub.development.application.service;

import com.jovine360.talenthub.activity.domain.event.AthleteActivityPublishedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Listens to AthleteActivityPublishedEvent and extracts performance rows
 * to create PerformanceMeasurement records with source=SYSTEM.
 */
@Component
public class ActivityPublishedEventListener {

    private final PerformanceMeasurementService measurementService;

    public ActivityPublishedEventListener(PerformanceMeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @ApplicationModuleListener
    void onActivityPublished(AthleteActivityPublishedEvent event) {
        if (event.performances() == null) return;
        for (var perf : event.performances()) {
            measurementService.recordSystemMeasurement(
                    event.athleteId(),
                    perf.metricCode(),
                    perf.measuredValue(),
                    perf.unit(),
                    event.activityDate(),
                    event.activityId());
        }
    }
}
