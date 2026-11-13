package com.jovine360.talenthub.scouting.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "evaluation_scores")
@Getter
@Setter
public class EvaluationScore extends AuditableEntity {

    @Column(name = "evaluation_id", columnDefinition = "CHAR(36)", nullable = false)
    private String evaluationId;

    @Column(name = "criterion_id", columnDefinition = "CHAR(36)", nullable = false)
    private String criterionId;

    @Column(name = "score", precision = 3, scale = 1)
    private BigDecimal score;

    @Column(name = "max_score", precision = 3, scale = 1)
    private BigDecimal maxScore = new BigDecimal("10.0");

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
