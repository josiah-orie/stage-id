package com.jovine360.talenthub.athlete.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Athlete-specific profile data extending the base user profile.
 * Each record links to a user account via userId.
 */
@Entity
@Table(name = "athlete_profiles")
@Getter
@Setter
public class AthleteProfile extends AuditableEntity {

    /** FK to identity.users — cross-module by ID. */
    @Column(name = "user_id", columnDefinition = "CHAR(36)", nullable = false, unique = true)
    private String userId;

    /** Human-readable athlete number (e.g. TH-00001 or FCB-FB-00042). */
    @Column(name = "athlete_number", length = 30, nullable = false, unique = true)
    private String athleteNumber;

    /** FK to sports.id — cross-module by ID. */
    @Column(name = "primary_sport_id", columnDefinition = "CHAR(36)")
    private String primarySportId;

    /** Sport code, cached for athlete number formatting. */
    @Column(name = "primary_sport_code", length = 10)
    private String primarySportCode;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    private Integer weightKg;

    @Column(name = "dominant_foot", length = 10)
    private String dominantFoot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private AthleteStatus status = AthleteStatus.ACTIVE;

    /** FK to organization.id — null if unaffiliated. */
    @Column(name = "organization_id", columnDefinition = "CHAR(36)")
    private String organizationId;

    @Column(name = "nationality", columnDefinition = "CHAR(2)")
    private String nationality;
}
