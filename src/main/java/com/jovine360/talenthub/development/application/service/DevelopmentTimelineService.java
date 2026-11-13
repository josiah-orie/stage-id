package com.jovine360.talenthub.development.application.service;

import com.jovine360.talenthub.development.api.DevelopmentTimelineResponse;
import com.jovine360.talenthub.development.api.DevelopmentTimelineResponse.TimelineEntry;
import com.jovine360.talenthub.development.domain.model.DevelopmentMilestone;
import com.jovine360.talenthub.development.domain.model.PerformanceMeasurement;
import com.jovine360.talenthub.development.domain.repository.DevelopmentMilestoneRepository;
import com.jovine360.talenthub.development.domain.repository.PerformanceMeasurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DevelopmentTimelineService {

    private final PerformanceMeasurementRepository measurementRepository;
    private final DevelopmentMilestoneRepository milestoneRepository;

    public DevelopmentTimelineService(PerformanceMeasurementRepository measurementRepository,
                                       DevelopmentMilestoneRepository milestoneRepository) {
        this.measurementRepository = measurementRepository;
        this.milestoneRepository = milestoneRepository;
    }

    public DevelopmentTimelineResponse getTimeline(String athleteId) {
        List<TimelineEntry> entries = new ArrayList<>();

        // Measurements
        for (PerformanceMeasurement m : measurementRepository.findActiveByAthleteId(athleteId)) {
            entries.add(new TimelineEntry(
                    m.getObservedAt().toString(),
                    "MEASUREMENT",
                    m.getMetricCode() + ": " + m.getMeasuredValue() + " " + (m.getUnit() != null ? m.getUnit() : ""),
                    m.getSource().name(),
                    m.getId()
            ));
        }

        // Milestones
        for (DevelopmentMilestone ms : milestoneRepository.findByAthleteIdOrderByMilestoneDateDesc(athleteId)) {
            entries.add(new TimelineEntry(
                    ms.getMilestoneDate().toString(),
                    "MILESTONE",
                    ms.getTitle(),
                    ms.getMilestoneType().name(),
                    ms.getId()
            ));
        }

        entries.sort(Comparator.comparing(TimelineEntry::date).reversed());
        return new DevelopmentTimelineResponse(athleteId, entries);
    }
}
