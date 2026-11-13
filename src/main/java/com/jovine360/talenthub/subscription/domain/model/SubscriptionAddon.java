package com.jovine360.talenthub.subscription.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscription_addons")
@Getter
@Setter
public class SubscriptionAddon extends AuditableEntity {

    @Column(name = "subscription_id", columnDefinition = "CHAR(36)", nullable = false)
    private String subscriptionId;

    @Column(name = "addon_code", length = 50, nullable = false)
    private String addonCode; // e.g. SCOUTING_SEATS

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
