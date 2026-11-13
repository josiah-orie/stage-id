package com.jovine360.talenthub.media.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "media_processing_jobs")
@Getter
@Setter
public class MediaProcessingJob extends BaseEntity {

    @Column(name = "media_asset_id", columnDefinition = "CHAR(36)", nullable = false)
    private String mediaAssetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 30, nullable = false)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private JobStatus status = JobStatus.QUEUED;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
