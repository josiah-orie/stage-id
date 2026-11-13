package com.jovine360.talenthub.activity.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "athlete_activity_performances")
@Getter
@Setter
public class AthleteActivityPerformance extends AuditableEntity {

    @Column(name = "activity_id", columnDefinition = "CHAR(36)", nullable = false)
    private String activityId;

    @Column(name = "metric_definition_id", columnDefinition = "CHAR(36)")
    private String metricDefinitionId;

    @Column(name = "metric_code", length = 60, nullable = false)
    private String metricCode;

    @Column(name = "measured_value", precision = 18, scale = 4)
    private BigDecimal measuredValue;

    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "measurement_context", length = 200)
    private String measurementContext;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20, nullable = false)
    private MeasurementSource source = MeasurementSource.SELF_REPORTED;
}
