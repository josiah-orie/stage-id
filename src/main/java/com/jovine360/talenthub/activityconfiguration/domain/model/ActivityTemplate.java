package com.jovine360.talenthub.activityconfiguration.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Template definition for an activity/session type.
 * Links an ActivityType to a Sport and defines the structure via ActivitySchemaVersions.
 */
@Entity
@Table(name = "activity_templates")
@Getter
@Setter
public class ActivityTemplate extends AuditableEntity {

    @Column(name = "activity_type_id", columnDefinition = "CHAR(36)", nullable = false)
    private String activityTypeId;

    @Column(name = "sport_id", columnDefinition = "CHAR(36)")
    private String sportId;

    @Column(name = "code", length = 30, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sport_code", length = 10)
    private String sportCode;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
