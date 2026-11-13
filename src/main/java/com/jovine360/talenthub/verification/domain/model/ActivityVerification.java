package com.jovine360.talenthub.verification.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "activity_verifications")
@Getter
@Setter
public class ActivityVerification extends BaseEntity {

    @Column(name = "activity_id", columnDefinition = "CHAR(36)", nullable = false)
    private String activityId;

    @Column(name = "verifier_id", columnDefinition = "CHAR(36)", nullable = false)
    private String verifierId;

    @Column(name = "verified_at", nullable = false)
    private Instant verifiedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
