package com.jovine360.talenthub.media.domain.event;

public record MediaUploadCompletedEvent(
        String mediaAssetId,
        String ownerId,
        String mimeType,
        String objectKey,
        String bucketName
) {}
