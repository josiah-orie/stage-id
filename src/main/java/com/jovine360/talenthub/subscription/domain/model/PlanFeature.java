package com.jovine360.talenthub.subscription.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plan_features")
@Getter
@Setter
public class PlanFeature extends AuditableEntity {

    @Column(name = "plan_id", columnDefinition = "CHAR(36)", nullable = false)
    private String planId;

    @Column(name = "entitlement_key", length = 80, nullable = false)
    private String entitlementKey;

    @Column(name = "limit_value")
    private Long limitValue;

    @Column(name = "unlimited", nullable = false)
    private boolean unlimited = false;
}
