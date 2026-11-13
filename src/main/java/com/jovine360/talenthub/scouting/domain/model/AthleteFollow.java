package com.jovine360.talenthub.scouting.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "athlete_follows")
@Getter
@Setter
public class AthleteFollow extends BaseEntity {

    @Column(name = "follower_id", columnDefinition = "CHAR(36)", nullable = false)
    private String followerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "follower_type", length = 20, nullable = false)
    private OwnerType followerType;

    @Column(name = "athlete_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteId;

    @Column(name = "followed_at", nullable = false)
    private Instant followedAt;

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = true;
}
