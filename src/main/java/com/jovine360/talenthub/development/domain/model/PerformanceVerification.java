package com.jovine360.talenthub.development.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "performance_verifications")
@Getter
@Setter
public class PerformanceVerification extends BaseEntity {

    @Column(name = "measurement_id", columnDefinition = "CHAR(36)", nullable = false)
    private String measurementId;

    @Column(name = "verified_by", columnDefinition = "CHAR(36)", nullable = false)
    private String verifiedBy;

    @Column(name = "verified_at", nullable = false)
    private Instant verifiedAt;

    @Column(name = "method", length = 100)
    private String method;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
