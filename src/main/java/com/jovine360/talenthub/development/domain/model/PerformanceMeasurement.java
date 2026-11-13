package com.jovine360.talenthub.development.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "performance_measurements")
@Getter
@Setter
public class PerformanceMeasurement extends AuditableEntity {

    @Column(name = "athlete_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteId;

    @Column(name = "athlete_sport_profile_id", columnDefinition = "CHAR(36)")
    private String athleteSportProfileId;

    @Column(name = "metric_definition_id", columnDefinition = "CHAR(36)")
    private String metricDefinitionId;

    @Column(name = "metric_code", length = 60, nullable = false)
    private String metricCode;

    @Column(name = "measured_value", precision = 18, scale = 4, nullable = false)
    private BigDecimal measuredValue;

    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "observed_at", nullable = false)
    private LocalDate observedAt;

    @Column(name = "measurement_context", length = 200)
    private String measurementContext;

    @Column(name = "source_activity_id", columnDefinition = "CHAR(36)")
    private String sourceActivityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20, nullable = false)
    private MeasurementSource source = MeasurementSource.SELF_REPORTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20, nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "verified_by", columnDefinition = "CHAR(36)")
    private String verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "recorded_by", columnDefinition = "CHAR(36)", nullable = false)
    private String recordedBy;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    /** Points to the row this corrects (non-null means this is a correction). */
    @Column(name = "corrected_from_id", columnDefinition = "CHAR(36)")
    private String correctedFromId;
}
