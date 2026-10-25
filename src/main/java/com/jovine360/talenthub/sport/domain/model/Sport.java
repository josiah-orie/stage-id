package com.jovine360.talenthub.sport.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a sport discipline supported on the platform.
 */
@Entity
@Table(name = "sports")
@Getter
@Setter
public class Sport extends AuditableEntity {

    /** Short unique code used in athlete numbers, e.g. "FB" for Football. */
    @Column(name = "code", length = 10, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
