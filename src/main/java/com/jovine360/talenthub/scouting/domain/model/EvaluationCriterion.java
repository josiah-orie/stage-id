package com.jovine360.talenthub.scouting.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "evaluation_criteria")
@Getter
@Setter
public class EvaluationCriterion extends AuditableEntity {

    @Column(name = "sport_id", columnDefinition = "CHAR(36)")
    private String sportId;

    @Column(name = "criterion_code", length = 60, nullable = false)
    private String criterionCode;

    @Column(name = "criterion_name", length = 150, nullable = false)
    private String criterionName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
