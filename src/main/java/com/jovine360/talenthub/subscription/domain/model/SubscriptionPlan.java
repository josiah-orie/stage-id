package com.jovine360.talenthub.subscription.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Subscription plan definition (e.g. FREE_TIER, BASIC, PROFESSIONAL).
 */
@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
public class SubscriptionPlan extends AuditableEntity {

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", length = 50, nullable = false)
    private PlanTargetRole targetRole;

    @Column(name = "price_per_month", precision = 10, scale = 2)
    private BigDecimal pricePerMonth;

    @Column(name = "currency", columnDefinition = "CHAR(3)")
    private String currency = "USD";

    /** JSON string of entitlement limits, e.g. {"max_athletes":5,"max_scouts":0} */
    @Column(name = "entitlements_json", columnDefinition = "TEXT")
    private String entitlementsJson;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
