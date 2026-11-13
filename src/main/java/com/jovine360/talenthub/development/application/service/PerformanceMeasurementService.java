package com.jovine360.talenthub.development.application.service;

import com.jovine360.talenthub.development.domain.event.PerformanceMeasurementRecordedEvent;
import com.jovine360.talenthub.development.domain.model.MeasurementSource;
import com.jovine360.talenthub.development.domain.model.PerformanceMeasurement;
import com.jovine360.talenthub.development.domain.model.VerificationStatus;
import com.jovine360.talenthub.development.domain.repository.PerformanceMeasurementRepository;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PerformanceMeasurementService {

    private final PerformanceMeasurementRepository measurementRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    public PerformanceMeasurementService(PerformanceMeasurementRepository measurementRepository,
                                          ApplicationEventPublisher eventPublisher,
                                          MeterRegistry meterRegistry) {
        this.measurementRepository = measurementRepository;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;
    }

    public PerformanceMeasurement recordMeasurement(String athleteId, String metricCode,
                                                     BigDecimal value, String unit,
                                                     LocalDate observedAt, String recordedBy,
                                                     MeasurementSource source) {
        PerformanceMeasurement m = new PerformanceMeasurement();
        m.setAthleteId(athleteId);
        m.setMetricCode(metricCode);
        m.setMeasuredValue(value);
        m.setUnit(unit);
        m.setObservedAt(observedAt);
        m.setRecordedBy(recordedBy);
        m.setRecordedAt(Instant.now());
        m.setSource(source);
        m.setVerificationStatus(VerificationStatus.UNVERIFIED);
        PerformanceMeasurement saved = measurementRepository.save(m);
        eventPublisher.publishEvent(new PerformanceMeasurementRecordedEvent(
                saved.getId(), athleteId, metricCode, value, unit, observedAt, source.name()));
        return saved;
    }

    public PerformanceMeasurement correctMeasurement(String originalId, BigDecimal correctedValue,
                                                      String correctedBy) {
        PerformanceMeasurement original = measurementRepository.findById(originalId)
                .orElseThrow(() -> new EntityNotFoundException("PerformanceMeasurement", originalId));
        PerformanceMeasurement correction = new PerformanceMeasurement();
        correction.setAthleteId(original.getAthleteId());
        correction.setAthleteSportProfileId(original.getAthleteSportProfileId());
        correction.setMetricCode(original.getMetricCode());
        correction.setMeasuredValue(correctedValue);
        correction.setUnit(original.getUnit());
        correction.setObservedAt(original.getObservedAt());
        correction.setRecordedBy(correctedBy);
        correction.setRecordedAt(Instant.now());
        correction.setSource(original.getSource());
        correction.setVerificationStatus(VerificationStatus.UNVERIFIED);
        correction.setCorrectedFromId(originalId);
        return measurementRepository.save(correction);
    }

    @Transactional(readOnly = true)
    public List<PerformanceMeasurement> getMeasurementHistory(String athleteId) {
        return measurementRepository.findActiveByAthleteId(athleteId);
    }

    @Transactional(readOnly = true)
    public List<PerformanceMeasurement> getChartData(String athleteId, String metricCode) {
        return measurementRepository.findChartData(athleteId, metricCode);
    }

    /** Called by AthleteActivityPublishedEvent listener to create SYSTEM measurements. */
    public PerformanceMeasurement recordSystemMeasurement(String athleteId, String metricCode,
                                                           BigDecimal value, String unit,
                                                           LocalDate observedAt, String sourceActivityId) {
        PerformanceMeasurement m = new PerformanceMeasurement();
        m.setAthleteId(athleteId);
        m.setMetricCode(metricCode);
        m.setMeasuredValue(value);
        m.setUnit(unit);
        m.setObservedAt(observedAt);
        m.setSourceActivityId(sourceActivityId);
        m.setRecordedBy("system");
        m.setRecordedAt(Instant.now());
        m.setSource(MeasurementSource.SYSTEM);
        m.setVerificationStatus(VerificationStatus.UNVERIFIED);
        return measurementRepository.save(m);
    }
}
