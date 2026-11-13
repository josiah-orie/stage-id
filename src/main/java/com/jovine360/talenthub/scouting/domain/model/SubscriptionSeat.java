package com.jovine360.talenthub.scouting.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "subscription_seats")
@Getter
@Setter
public class SubscriptionSeat extends BaseEntity {

    @Column(name = "subscription_id", columnDefinition = "CHAR(36)", nullable = false)
    private String subscriptionId;

    @Column(name = "seat_type", length = 20, nullable = false)
    private String seatType = "SCOUT";

    @Column(name = "assigned_to_user_id", columnDefinition = "CHAR(36)")
    private String assignedToUserId;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
