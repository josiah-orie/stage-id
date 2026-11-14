package com.jovine360.talenthub.organization.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** An athlete's affiliation with an organization. Approved by the athlete. */
@Entity
@Table(name = "organization_athlete_relationships")
@Getter
@Setter
public class OrganizationAthleteRelationship extends AuditableEntity {

    @Column(name = "organization_id", columnDefinition = "CHAR(36)", nullable = false)
    private String organizationId;

    @Column(name = "athlete_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteUserId;

    /** Access level the organization's staff get (an AthletePermission name). */
    @Column(name = "permission", length = 30, nullable = false)
    private String permission;

    /** PENDING, ACTIVE, REJECTED, ENDED. */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING";

    @Column(name = "requested_by", columnDefinition = "CHAR(36)", nullable = false)
    private String requestedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "ended_at")
    private Instant endedAt;
}
