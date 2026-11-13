package com.jovine360.talenthub.activityconfiguration.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "activity_types")
@Getter
@Setter
public class ActivityType extends AuditableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "code", length = 30, nullable = false, unique = true)
    private ActivityTypeCode code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
