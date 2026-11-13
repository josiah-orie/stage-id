package com.jovine360.talenthub.activityconfiguration.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "activity_field_definitions")
@Getter
@Setter
public class ActivityFieldDefinition extends AuditableEntity {

    @Column(name = "schema_version_id", columnDefinition = "CHAR(36)", nullable = false)
    private String schemaVersionId;

    @Column(name = "field_code", length = 60, nullable = false)
    private String fieldCode;

    @Column(name = "field_label", length = 120, nullable = false)
    private String fieldLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", length = 30, nullable = false)
    private FieldType fieldType;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "validation_rules_json", columnDefinition = "TEXT")
    private String validationRulesJson;

    @Column(name = "help_text", columnDefinition = "TEXT")
    private String helpText;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
