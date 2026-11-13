package com.jovine360.talenthub.safeguarding.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "safeguarding_reports")
@Getter
@Setter
public class SafeguardingReport extends BaseEntity {

    @Column(name = "reporter_id", columnDefinition = "CHAR(36)", nullable = false)
    private String reporterId;

    @Column(name = "reported_entity_id", columnDefinition = "CHAR(36)")
    private String reportedEntityId;

    @Column(name = "reported_entity_type", length = 50)
    private String reportedEntityType;

    @Column(name = "report_type", length = 40, nullable = false)
    private String reportType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING";

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by", columnDefinition = "CHAR(36)")
    private String resolvedBy;
}
