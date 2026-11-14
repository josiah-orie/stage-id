package com.jovine360.talenthub.media.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "media_assets")
@Getter
@Setter
public class MediaAsset extends AuditableEntity {

    @Column(name = "owner_id", columnDefinition = "CHAR(36)", nullable = false)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", length = 20, nullable = false)
    private OwnerType ownerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", length = 30, nullable = false)
    private AssetType assetType;

    @Column(name = "original_filename", length = 500)
    private String originalFilename;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "bucket_name", length = 100)
    private String bucketName;

    @Column(name = "object_key", length = 1000)
    private String objectKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", length = 20, nullable = false)
    private ProcessingStatus processingStatus = ProcessingStatus.INITIATED;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @Column(name = "uploaded_by", columnDefinition = "CHAR(36)")
    private String uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", length = 20, nullable = false)
    private ModerationStatus moderationStatus = ModerationStatus.CLEAN;

    @Column(name = "moderation_note", length = 500)
    private String moderationNote;

    @Column(name = "moderated_by", columnDefinition = "CHAR(36)")
    private String moderatedBy;

    @Column(name = "moderated_at")
    private Instant moderatedAt;
}
