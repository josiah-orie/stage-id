package com.jovine360.talenthub.media.domain.repository;

import com.jovine360.talenthub.media.domain.model.MediaAsset;
import com.jovine360.talenthub.media.domain.model.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, String> {
    List<MediaAsset> findByOwnerIdAndProcessingStatusNot(String ownerId, ProcessingStatus status);
}
