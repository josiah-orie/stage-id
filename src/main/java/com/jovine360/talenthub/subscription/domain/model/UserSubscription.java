package com.jovine360.talenthub.subscription.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Links a subscriber (user or org) to a subscription plan with lifecycle dates.
 */
@Entity
@Table(name = "user_subscriptions")
@Getter
@Setter
public class UserSubscription extends AuditableEntity {

    /** userId or orgId — cross-module reference by ID only. */
    @Column(name = "subscriber_id", columnDefinition = "CHAR(36)", nullable = false)
    private String subscriberId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false, columnDefinition = "CHAR(36)")
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE
                && (expiresAt == null || Instant.now().isBefore(expiresAt));
    }
}
