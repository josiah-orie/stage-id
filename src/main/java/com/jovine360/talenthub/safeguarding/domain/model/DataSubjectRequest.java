package com.jovine360.talenthub.safeguarding.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "data_subject_requests")
@Getter
@Setter
public class DataSubjectRequest extends BaseEntity {

    @Column(name = "requester_id", columnDefinition = "CHAR(36)", nullable = false)
    private String requesterId;

    @Column(name = "request_type", length = 20, nullable = false)
    private String requestType; // ACCESS / DELETION / CORRECTION

    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING";

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
