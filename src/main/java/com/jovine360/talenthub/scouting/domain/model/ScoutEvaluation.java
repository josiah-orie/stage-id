package com.jovine360.talenthub.scouting.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "scout_evaluations")
@Getter
@Setter
public class ScoutEvaluation extends AuditableEntity {

    @Column(name = "scout_profile_id", columnDefinition = "CHAR(36)", nullable = false)
    private String scoutProfileId;

    @Column(name = "athlete_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteId;

    @Column(name = "athlete_sport_profile_id", columnDefinition = "CHAR(36)")
    private String athleteSportProfileId;

    @Column(name = "sport_id", columnDefinition = "CHAR(36)")
    private String sportId;

    @Column(name = "evaluation_title", length = 200, nullable = false)
    private String evaluationTitle;

    @Column(name = "overall_rating", precision = 3, scale = 1)
    private BigDecimal overallRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private EvaluationStatus status = EvaluationStatus.DRAFT;

    @Column(name = "evaluation_date")
    private LocalDate evaluationDate;
}
