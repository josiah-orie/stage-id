package com.jovine360.talenthub.scout.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "scout_verifications")
@Getter
@Setter
public class ScoutVerification extends BaseEntity {

    @Column(name = "scout_profile_id", columnDefinition = "CHAR(36)", nullable = false)
    private String scoutProfileId;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "reviewed_by", columnDefinition = "CHAR(36)")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ScoutVerificationStatus status = ScoutVerificationStatus.PENDING;

    @Column(name = "supporting_document_media_id", columnDefinition = "CHAR(36)")
    private String supportingDocumentMediaId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
