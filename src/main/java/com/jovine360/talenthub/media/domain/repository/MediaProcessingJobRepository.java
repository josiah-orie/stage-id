package com.jovine360.talenthub.media.domain.repository;

import com.jovine360.talenthub.media.domain.model.MediaProcessingJob;
import com.jovine360.talenthub.media.domain.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaProcessingJobRepository extends JpaRepository<MediaProcessingJob, String> {
    List<MediaProcessingJob> findByMediaAssetId(String mediaAssetId);
    List<MediaProcessingJob> findByStatusAndAttemptsLessThan(JobStatus status, int maxAttempts);
}
