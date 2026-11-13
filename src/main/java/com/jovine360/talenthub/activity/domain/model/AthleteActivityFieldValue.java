package com.jovine360.talenthub.activity.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "athlete_activity_field_values")
@Getter
@Setter
public class AthleteActivityFieldValue extends AuditableEntity {

    @Column(name = "activity_id", columnDefinition = "CHAR(36)", nullable = false)
    private String activityId;

    @Column(name = "field_definition_id", columnDefinition = "CHAR(36)", nullable = false)
    private String fieldDefinitionId;

    @Column(name = "field_code", length = 60, nullable = false)
    private String fieldCode;

    @Column(name = "string_value", columnDefinition = "TEXT")
    private String stringValue;

    @Column(name = "numeric_value", precision = 18, scale = 4)
    private BigDecimal numericValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "date_value")
    private LocalDate dateValue;

    @Column(name = "json_value", columnDefinition = "TEXT")
    private String jsonValue;
}
