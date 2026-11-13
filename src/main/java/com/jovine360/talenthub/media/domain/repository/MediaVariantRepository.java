package com.jovine360.talenthub.media.domain.repository;

import com.jovine360.talenthub.media.domain.model.MediaVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaVariantRepository extends JpaRepository<MediaVariant, String> {
    List<MediaVariant> findByMediaAssetId(String mediaAssetId);
}
