package com.jovine360.talenthub.discovery.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Flat denormalized projection for athlete discovery search.
 * Updated via event listeners from athlete, activity, and development modules.
 */
@Entity
@Table(name = "athlete_search_projections")
@Getter
@Setter
public class AthleteSearchProjection extends BaseEntity {

    @Column(name = "athlete_id", columnDefinition = "CHAR(36)", nullable = false, unique = true)
    private String athleteId;

    @Column(name = "athlete_number", length = 30)
    private String athleteNumber;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "sport_ids_json", columnDefinition = "TEXT")
    private String sportIdsJson;

    @Column(name = "discipline_ids_json", columnDefinition = "TEXT")
    private String disciplineIdsJson;

    @Column(name = "position_ids_json", columnDefinition = "TEXT")
    private String positionIdsJson;

    @Column(name = "country_of_residence", columnDefinition = "CHAR(2)")
    private String countryOfResidence;

    @Column(name = "state_region", length = 100)
    private String stateRegion;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_category", length = 20)
    private AgeCategory ageCategory;

    @Column(name = "verification_status", length = 20)
    private String verificationStatus;

    @Column(name = "profile_status", length = 20)
    private String profileStatus;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "top_metrics_json", columnDefinition = "TEXT")
    private String topMetricsJson;

    @Column(name = "visibility_level", length = 20)
    private String visibilityLevel = "PUBLIC";

    @Column(name = "updated_at")
    private java.time.Instant updatedAt;
}
