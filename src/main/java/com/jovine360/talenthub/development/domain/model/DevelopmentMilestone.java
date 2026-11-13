package com.jovine360.talenthub.development.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "development_milestones")
@Getter
@Setter
public class DevelopmentMilestone extends AuditableEntity {

    @Column(name = "athlete_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteId;

    @Column(name = "sport_id", columnDefinition = "CHAR(36)")
    private String sportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "milestone_type", length = 30, nullable = false)
    private MilestoneType milestoneType;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "milestone_date", nullable = false)
    private LocalDate milestoneDate;

    @Column(name = "evidence_activity_id", columnDefinition = "CHAR(36)")
    private String evidenceActivityId;

    @Column(name = "created_by", columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    private String createdBy;
}
