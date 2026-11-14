package com.jovine360.talenthub.media.application.service;

import com.jovine360.talenthub.athleteaccess.api.AthleteAccessPolicy;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
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
import java.util.Collection;
import java.util.Set;

@Service
@Transactional
public class MediaAssetService {

    private static final Set<AssetType> SCOUT_VISIBLE_ASSETS =
            Set.of(AssetType.ACTIVITY_VIDEO, AssetType.VIDEO_THUMBNAIL, AssetType.PROFILE_IMAGE);

    private static final Set<String> ALLOWED_MIMES = Set.of(
            "image/jpeg", "image/png", "video/mp4", "video/quicktime");

    @Value("${media.max-video-size-bytes:524288000}")
    private long maxVideoSizeBytes;

    private final MediaAssetRepository mediaAssetRepository;
    private final MediaUploadSessionRepository sessionRepository;
    private final MediaAccessAuditRepository auditRepository;
    private final MediaStorageService storageService;
    private final ApplicationEventPublisher eventPublisher;
    private final AthleteAccessPolicy accessPolicy;

    public MediaAssetService(MediaAssetRepository mediaAssetRepository,
                              MediaUploadSessionRepository sessionRepository,
                              MediaAccessAuditRepository auditRepository,
                              MediaStorageService storageService,
                              ApplicationEventPublisher eventPublisher,
                              AthleteAccessPolicy accessPolicy) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.sessionRepository = sessionRepository;
        this.auditRepository = auditRepository;
        this.storageService = storageService;
        this.eventPublisher = eventPublisher;
        this.accessPolicy = accessPolicy;
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
                                                   String mimeType, String uploadedBy,
                                                   Collection<String> actorRoles) {
        assertCanUpload(ownerId, ownerType, uploadedBy, actorRoles);
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
    public MediaAsset completeUpload(String assetId, String actorId, Collection<String> actorRoles) {
        MediaAsset asset = mediaAssetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("MediaAsset", assetId));
        assertCanManage(asset, actorId, actorRoles);
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
    public String getPlaybackUrl(String assetId, String accessedBy, Collection<String> actorRoles) {
        MediaAsset asset = mediaAssetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("MediaAsset", assetId));
        assertCanView(asset, accessedBy, actorRoles);
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

    @Transactional(readOnly = true)
    public MediaAsset getById(String assetId, String actorId, Collection<String> actorRoles) {
        MediaAsset asset = getById(assetId);
        assertCanView(asset, actorId, actorRoles);
        return asset;
    }

    @Transactional
    public void softDelete(String assetId, String actorId, Collection<String> actorRoles) {
        MediaAsset asset = mediaAssetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("MediaAsset", assetId));
        assertCanManage(asset, actorId, actorRoles);
        asset.setProcessingStatus(ProcessingStatus.DELETED);
        asset.setDeletedAt(Instant.now());
        mediaAssetRepository.save(asset);
        storageService.softDelete(asset.getBucketName(), asset.getObjectKey());
    }

    // ---- moderation ------------------------------------------------------------------------

    /** Marks an asset FLAGGED after it is reported. Restricted assets stay restricted. */
    public void flag(String assetId, String note) {
        mediaAssetRepository.findById(assetId).ifPresent(asset -> {
            if (asset.getModerationStatus() == ModerationStatus.CLEAN) {
                asset.setModerationStatus(ModerationStatus.FLAGGED);
                asset.setModerationNote(note);
                mediaAssetRepository.save(asset);
            }
        });
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<MediaAsset> listByModerationStatus(
            ModerationStatus status, org.springframework.data.domain.Pageable pageable) {
        return mediaAssetRepository.findByModerationStatus(status, pageable);
    }

    /** Admin decision. {@code CLEAN} approves; {@code RESTRICTED} hides the asset. */
    public MediaAsset moderate(String assetId, ModerationStatus decision, String note, String adminId) {
        if (decision == ModerationStatus.FLAGGED) {
            throw new IllegalArgumentException("An admin decision must be CLEAN or RESTRICTED");
        }
        MediaAsset asset = getById(assetId);
        if (asset.getModerationStatus() == decision) {
            throw new com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException(
                    "MediaAsset", asset.getModerationStatus().name(), decision.name());
        }
        asset.setModerationStatus(decision);
        asset.setModerationNote(note);
        asset.setModeratedBy(adminId);
        asset.setModeratedAt(Instant.now());
        return mediaAssetRepository.save(asset);
    }

    // ---- authorisation -----------------------------------------------------------------------

    private void assertCanUpload(String ownerId, OwnerType ownerType, String actorId, Collection<String> roles) {
        if (!canManageOwner(ownerId, ownerType, actorId, roles)) {
            throw new SecurityException("You cannot upload media for this owner");
        }
    }

    private void assertCanManage(MediaAsset asset, String actorId, Collection<String> roles) {
        boolean uploader = actorId != null && actorId.equals(asset.getUploadedBy());
        if (!uploader && !canManageOwner(asset.getOwnerId(), asset.getOwnerType(), actorId, roles)) {
            throw new SecurityException("You cannot modify this media asset");
        }
    }

    private void assertCanView(MediaAsset asset, String actorId, Collection<String> roles) {
        boolean uploader = actorId != null && actorId.equals(asset.getUploadedBy());
        if (uploader || canManageOwner(asset.getOwnerId(), asset.getOwnerType(), actorId, roles)) return;
        if (asset.getModerationStatus() == ModerationStatus.RESTRICTED) {
            throw new SecurityException("This media asset is not accessible.");
        }
        if (asset.getAssetType() == AssetType.ORG_LOGO) return; // logos are public
        if (asset.getOwnerType() == OwnerType.ATHLETE) {
            if (accessPolicy.canAccess(asset.getOwnerId(), actorId, roles, AthletePermission.VIEW_STATS)) return;
            if (roles.contains("SCOUT") && SCOUT_VISIBLE_ASSETS.contains(asset.getAssetType())) return;
        }
        throw new SecurityException("You cannot access this media asset");
    }

    private boolean canManageOwner(String ownerId, OwnerType ownerType, String actorId, Collection<String> roles) {
        if (roles.contains("ADMIN") || roles.contains("SUPER_ADMIN")) return true;
        return switch (ownerType) {
            case ATHLETE -> accessPolicy.canAccess(ownerId, actorId, roles, AthletePermission.MANAGE);
            case USER -> ownerId.equals(actorId);
            case ORGANIZATION -> roles.contains("ORG_ADMIN");
        };
    }

    public record InitiateUploadResponse(String assetId, String presignedUrl, Instant expiry) {}
}
