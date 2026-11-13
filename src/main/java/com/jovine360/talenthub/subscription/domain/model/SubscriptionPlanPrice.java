package com.jovine360.talenthub.subscription.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plan_prices")
@Getter
@Setter
public class SubscriptionPlanPrice extends AuditableEntity {

    @Column(name = "plan_id", columnDefinition = "CHAR(36)", nullable = false)
    private String planId;

    @Column(name = "currency", columnDefinition = "CHAR(3)", nullable = false)
    private String currency;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_period", length = 20, nullable = false)
    private BillingPeriod billingPeriod;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
