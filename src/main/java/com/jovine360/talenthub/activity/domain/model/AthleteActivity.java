package com.jovine360.talenthub.activity.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "athlete_activities")
@Getter
@Setter
public class AthleteActivity extends AuditableEntity {

    @Column(name = "athlete_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteId;

    @Column(name = "athlete_sport_profile_id", columnDefinition = "CHAR(36)")
    private String athleteSportProfileId;

    @Column(name = "sport_id", columnDefinition = "CHAR(36)")
    private String sportId;

    @Column(name = "activity_type_id", columnDefinition = "CHAR(36)", nullable = false)
    private String activityTypeId;

    @Column(name = "schema_version_id", columnDefinition = "CHAR(36)", nullable = false)
    private String schemaVersionId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "organization_id", columnDefinition = "CHAR(36)")
    private String organizationId;

    @Column(name = "submitted_by", columnDefinition = "CHAR(36)")
    private String submittedBy;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_level", length = 20, nullable = false)
    private VisibilityLevel visibilityLevel = VisibilityLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_status", length = 20, nullable = false)
    private ActivityStatus activityStatus = ActivityStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20, nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;
}
