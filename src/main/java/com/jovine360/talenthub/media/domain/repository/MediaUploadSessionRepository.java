package com.jovine360.talenthub.media.domain.repository;

import com.jovine360.talenthub.media.domain.model.MediaUploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaUploadSessionRepository extends JpaRepository<MediaUploadSession, String> {
    Optional<MediaUploadSession> findByMediaAssetId(String mediaAssetId);
}
