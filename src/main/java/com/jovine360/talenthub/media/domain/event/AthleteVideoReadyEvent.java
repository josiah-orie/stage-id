package com.jovine360.talenthub.media.domain.event;

public record AthleteVideoReadyEvent(
        String mediaAssetId,
        String athleteId,
        String thumbnailObjectKey,
        String mp4ObjectKey
) {}
