package com.jovine360.talenthub.media.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "media_upload_sessions")
@Getter
@Setter
public class MediaUploadSession extends BaseEntity {

    @Column(name = "media_asset_id", columnDefinition = "CHAR(36)", nullable = false)
    private String mediaAssetId;

    @Column(name = "presigned_url", columnDefinition = "TEXT")
    private String presignedUrl;

    @Column(name = "presigned_expiry")
    private Instant presignedExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", length = 20, nullable = false)
    private SessionStatus sessionStatus = SessionStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
