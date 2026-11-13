package com.jovine360.talenthub.activityconfiguration.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "activity_schema_versions")
@Getter
@Setter
public class ActivitySchemaVersion extends AuditableEntity {

    @Column(name = "template_id", columnDefinition = "CHAR(36)", nullable = false)
    private String templateId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SchemaStatus status = SchemaStatus.DRAFT;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by", columnDefinition = "CHAR(36)")
    private String publishedBy;
}
