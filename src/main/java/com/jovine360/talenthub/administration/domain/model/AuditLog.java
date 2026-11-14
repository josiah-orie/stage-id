package com.jovine360.talenthub.administration.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Row in the {@code audit_logs} table created by V1. */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog extends BaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "actor_id", columnDefinition = "CHAR(36)")
    private String actorId;

    @Column(name = "actor_role", length = 50)
    private String actorRole;

    @Column(name = "action", length = 100, nullable = false)
    private String action;

    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    @Column(name = "entity_id", length = 36)
    private String entityId;

    @Column(name = "after_state", columnDefinition = "LONGTEXT")
    private String details;
}
