package com.jovine360.talenthub.activityconfiguration.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "activity_validation_rules")
@Getter
@Setter
public class ActivityValidationRule extends AuditableEntity {

    @Column(name = "field_definition_id", columnDefinition = "CHAR(36)", nullable = false)
    private String fieldDefinitionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", length = 20, nullable = false)
    private ValidationRuleType ruleType;

    @Column(name = "rule_value", length = 500)
    private String ruleValue;

    @Column(name = "error_message", length = 300)
    private String errorMessage;
}
