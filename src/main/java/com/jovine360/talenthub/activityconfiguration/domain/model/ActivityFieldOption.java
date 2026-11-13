package com.jovine360.talenthub.activityconfiguration.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "activity_field_options")
@Getter
@Setter
public class ActivityFieldOption extends AuditableEntity {

    @Column(name = "field_definition_id", columnDefinition = "CHAR(36)", nullable = false)
    private String fieldDefinitionId;

    @Column(name = "option_value", length = 100, nullable = false)
    private String optionValue;

    @Column(name = "display_label", length = 150, nullable = false)
    private String displayLabel;

    @Column(name = "ordinal", nullable = false)
    private int ordinal;
}
