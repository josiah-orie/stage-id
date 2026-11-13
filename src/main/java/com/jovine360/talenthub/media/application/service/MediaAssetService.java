package com.jovine360.talenthub.media.application.service;

import com.jovine360.talenthub.media.domain.event.MediaUploadCompletedEvent;
import com.jovine360.talenthub.media.domain.model.*;
import com.jovine360.talenthub.media.domain.repository.MediaAccessAuditRepository;
import com.jovine360.talenthub.media.domain.repository.MediaAssetRepository;
import com.jovine360.talenthub.media.domain.repository.MediaUploadSessionRepository;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class MediaAssetService {

    private static final Set<String> ALLOWED_MIMES = Set.of(
            "image/jpeg", "image/png", "video/mp4", "video/quicktime");

    @Value("${media.max-video-size-bytes:524288000}")
    private long maxVideoSizeBytes;

    private final MediaAssetRepository mediaAssetRepository;
    private final MediaUploadSessionRepository sessionRepository;
    private final MediaAccessAuditRepository auditRepository;
    private final MediaStorageService storageService;
    private final ApplicationEventPublisher eventPublisher;

    public MediaAssetService(MediaAssetRepository mediaAssetRepository,
                              MediaUploadSessionRepository sessionRepository,
                              MediaAccessAuditRepository auditRepository,
                              MediaStorageService storageService,
                              ApplicationEventPublisher eventPublisher) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.sessionRepository = sessionRepository;
        this.auditRepository = auditRepository;
        this.storageService = storageService;
        this.eventPublisher = eventPublisher;
    }

    /** Validates MIME type. */
    public void validateMimeType(String mimeType) {
        if (!ALLOWED_MIMES.contains(mimeType)) {
            throw new IllegalArgumentException("Unsupported media type: " + mimeType);
        }
    }

    /** Initiates upload: creates MediaAsset(INITIATED) + MediaUploadSession with 15-min presigned PUT URL. */
    public InitiateUploadResponse initiateUpload(String ownerId, OwnerType ownerType,
                                                   AssetType assetType, String originalFilename,
                                                   String mimeType, String uploadedBy) {
        validateMimeType(mimeType);

        String bucket = ownerType == OwnerType.ATHLETE ? "athlete-media"
                : ownerType == OwnerType.ORGANIZATION ? "org-media" : "private-docs";
        String objectKey = ownerId + "/" + java.util.UUID.randomUUID() + "/" + originalFilename;

        MediaAsset asset = new MediaAsset();
        asset.setOwnerId(ownerId);
        asset.setOwnerType(ownerType);
        asset.setAssetType(assetType);
        asset.setOriginalFilename(originalFilename);
        asset.setMimeType(mimeType);
        asset.setBucketName(bucket);
        asset.setObjectKey(objectKey);
        asset.setProcessingStatus(ProcessingStatus.INITIATED);
        asset.setUploadedBy(uploadedBy);
        MediaAsset savedAsset = mediaAssetRepository.save(asset);

        String presignedUrl = storageService.generatePresignedUploadUrl(bucket, objectKey);
        Instant expiry = Instant.now().plusSeconds(900);

        MediaUploadSession session = new MediaUploadSession();
        session.setMediaAssetId(savedAsset.getId());
        session.setPresignedUrl(presignedUrl);
        session.setPresignedExpiry(expiry);
        session.setSessionStatus(SessionStatus.PENDING);
        session.setCreatedAt(Instant.now());
        sessionRepository.save(session);

        return new InitiateUploadResponse(savedAsset.getId(), presignedUrl, expiry);
    }

    /** Verifies the object exists in MinIO, sets UPLOADED, publishes MediaUploadCompletedEvent. */
    public MediaAsset completeUpload(String assetId) {
        MediaAsset asset = mediaAssetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("MediaAsset", assetId));
        if (!storageService.objectExists(asset.getBucketName(), asset.getObjectKey())) {
            throw new IllegalStateException("Object not found in storage for asset: " + assetId);
        }
        asset.setProcessingStatus(ProcessingStatus.UPLOADED);
        asset.setUploadedAt(Instant.now());
        MediaAsset saved = mediaAssetRepository.save(asset);

        sessionRepository.findByMediaAssetId(assetId).ifPresent(s -> {
            s.setSessionStatus(SessionStatus.COMPLETED);
            s.setCompletedAt(Instant.now());
            sessionRepository.save(s);
        });

        eventPublisher.publishEvent(new MediaUploadCompletedEvent(
                saved.getId(), saved.getOwnerId(), saved.getMimeType(),
                saved.getObjectKey(), saved.getBucketName()));
        return saved;
    }

    /** Generates a 15-min presigned GET URL and writes MediaAccessAudit. */
    @Transactional
    public String getPlaybackUrl(String assetId, String accessedBy) {
        MediaAsset asset = mediaAssetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("MediaAsset", assetId));
        if (asset.getProcessingStatus() != ProcessingStatus.READY) {
            throw new IllegalStateException("Media asset is not ready: " + assetId);
        }
        String url = storageService.generatePresignedDownloadUrl(asset.getBucketName(), asset.getObjectKey());
        MediaAccessAudit audit = new MediaAccessAudit();
        audit.setMediaAssetId(assetId);
        audit.setAccessedBy(accessedBy);
        audit.setAccessedAt(Instant.now());
        audit.setAccessType("PRESIGNED_VIEW");
        auditRepository.save(audit);
        return url;
    }

    @Transactional(readOnly = true)
    public MediaAsset getById(String assetId) {
        return mediaAssetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("MediaAsset", assetId));
    }

    @Transactional
    public void softDelete(String assetId) {
        MediaAsset asset = mediaAssetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("MediaAsset", assetId));
        asset.setProcessingStatus(ProcessingStatus.DELETED);
        asset.setDeletedAt(Instant.now());
        mediaAssetRepository.save(asset);
        storageService.softDelete(asset.getBucketName(), asset.getObjectKey());
    }

    public record InitiateUploadResponse(String assetId, String presignedUrl, Instant expiry) {}
}
