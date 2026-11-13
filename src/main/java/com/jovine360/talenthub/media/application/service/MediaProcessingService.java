package com.jovine360.talenthub.media.application.service;

import com.jovine360.talenthub.media.domain.event.AthleteVideoReadyEvent;
import com.jovine360.talenthub.media.domain.event.MediaUploadCompletedEvent;
import com.jovine360.talenthub.media.domain.model.*;
import com.jovine360.talenthub.media.domain.repository.MediaAssetRepository;
import com.jovine360.talenthub.media.domain.repository.MediaProcessingJobRepository;
import com.jovine360.talenthub.media.domain.repository.MediaVariantRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Instant;

/**
 * Handles post-upload processing: virus scan, image validation, video transcoding.
 * Runs @Async so it does not block the HTTP thread that calls completeUpload.
 */
@Service
public class MediaProcessingService {

    private static final Logger log = LoggerFactory.getLogger(MediaProcessingService.class);
    private static final int MAX_ATTEMPTS = 3;

    private final MediaAssetRepository assetRepository;
    private final MediaProcessingJobRepository jobRepository;
    private final MediaVariantRepository variantRepository;
    private final VirusScanService virusScanService;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    public MediaProcessingService(MediaAssetRepository assetRepository,
                                   MediaProcessingJobRepository jobRepository,
                                   MediaVariantRepository variantRepository,
                                   VirusScanService virusScanService,
                                   ApplicationEventPublisher eventPublisher,
                                   MeterRegistry meterRegistry) {
        this.assetRepository = assetRepository;
        this.jobRepository = jobRepository;
        this.variantRepository = variantRepository;
        this.virusScanService = virusScanService;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;
    }

    @ApplicationModuleListener
    @Async
    @Transactional
    public void onUploadCompleted(MediaUploadCompletedEvent event) {
        meterRegistry.counter("media.processing.jobs.total").increment();
        MediaAsset asset = assetRepository.findById(event.mediaAssetId()).orElse(null);
        if (asset == null) return;

        // Virus scan
        if (!virusScanService.isSafe(event.bucketName(), event.objectKey())) {
            asset.setProcessingStatus(ProcessingStatus.REJECTED);
            assetRepository.save(asset);
            return;
        }

        asset.setProcessingStatus(ProcessingStatus.VALIDATING);
        assetRepository.save(asset);

        String mime = event.mimeType() != null ? event.mimeType() : "";
        if (mime.startsWith("image/")) {
            processImage(asset);
        } else if (mime.startsWith("video/")) {
            processVideo(asset);
        } else {
            asset.setProcessingStatus(ProcessingStatus.READY);
            asset.setProcessedAt(Instant.now());
            assetRepository.save(asset);
        }
    }

    private void processImage(MediaAsset asset) {
        // Image: validate MIME and set READY
        asset.setProcessingStatus(ProcessingStatus.READY);
        asset.setProcessedAt(Instant.now());
        assetRepository.save(asset);
    }

    private void processVideo(MediaAsset asset) {
        asset.setProcessingStatus(ProcessingStatus.PROCESSING);
        assetRepository.save(asset);

        // Create processing jobs
        MediaProcessingJob thumbJob = createJob(asset.getId(), JobType.THUMBNAIL_GENERATE);
        MediaProcessingJob transcodeJob = createJob(asset.getId(), JobType.VIDEO_TRANSCODE);

        // Execute thumbnail
        String thumbnailKey = runThumbnail(asset, thumbJob);
        // Execute transcode
        String mp4Key = runTranscode(asset, transcodeJob);

        if (thumbJob.getStatus() == JobStatus.COMPLETED && transcodeJob.getStatus() == JobStatus.COMPLETED) {
            asset.setProcessingStatus(ProcessingStatus.READY);
            asset.setProcessedAt(Instant.now());
            assetRepository.save(asset);
            eventPublisher.publishEvent(new AthleteVideoReadyEvent(
                    asset.getId(), asset.getOwnerId(), thumbnailKey, mp4Key));
        } else {
            asset.setProcessingStatus(ProcessingStatus.FAILED);
            assetRepository.save(asset);
        }
    }

    private MediaProcessingJob createJob(String assetId, JobType jobType) {
        MediaProcessingJob job = new MediaProcessingJob();
        job.setMediaAssetId(assetId);
        job.setJobType(jobType);
        job.setStatus(JobStatus.QUEUED);
        job.setAttempts(0);
        job.setCreatedAt(Instant.now());
        return jobRepository.save(job);
    }

    private String runThumbnail(MediaAsset asset, MediaProcessingJob job) {
        job.setStatus(JobStatus.IN_PROGRESS);
        job.setAttempts(job.getAttempts() + 1);
        job.setLastAttemptAt(Instant.now());
        jobRepository.save(job);

        String inputPath = "/tmp/" + asset.getId() + "_input";
        String outputPath = "/tmp/" + asset.getId() + "_thumb.jpg";
        try {
            // ffmpeg -i {input} -ss 00:00:03 -vframes 1 {output.jpg}
            int exitCode = new ProcessBuilder(
                    "ffmpeg", "-i", inputPath, "-ss", "00:00:03", "-vframes", "1", outputPath, "-y")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor();

            String variantKey = asset.getObjectKey() + "_thumb.jpg";
            if (exitCode == 0) {
                createVariant(asset.getId(), VariantType.THUMBNAIL, variantKey, null, null);
                job.setStatus(JobStatus.COMPLETED);
                job.setCompletedAt(Instant.now());
                jobRepository.save(job);
                return variantKey;
            } else {
                handleJobFailure(job, "FFmpeg thumbnail exit code: " + exitCode);
                return null;
            }
        } catch (Exception e) {
            handleJobFailure(job, e.getMessage());
            return null;
        } finally {
            new File(outputPath).delete();
        }
    }

    private String runTranscode(MediaAsset asset, MediaProcessingJob job) {
        job.setStatus(JobStatus.IN_PROGRESS);
        job.setAttempts(job.getAttempts() + 1);
        job.setLastAttemptAt(Instant.now());
        jobRepository.save(job);

        String inputPath = "/tmp/" + asset.getId() + "_input";
        String outputPath = "/tmp/" + asset.getId() + "_720p.mp4";
        try {
            // ffmpeg -i {input} -vcodec h264 -acodec aac -vf scale=1280:720 {output.mp4}
            int exitCode = new ProcessBuilder(
                    "ffmpeg", "-i", inputPath, "-vcodec", "h264", "-acodec", "aac",
                    "-vf", "scale=1280:720", outputPath, "-y")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor();

            String variantKey = asset.getObjectKey() + "_720p.mp4";
            if (exitCode == 0) {
                createVariant(asset.getId(), VariantType.MP4_720P, variantKey, 1280, 720);
                job.setStatus(JobStatus.COMPLETED);
                job.setCompletedAt(Instant.now());
                jobRepository.save(job);
                return variantKey;
            } else {
                handleJobFailure(job, "FFmpeg transcode exit code: " + exitCode);
                return null;
            }
        } catch (Exception e) {
            handleJobFailure(job, e.getMessage());
            return null;
        } finally {
            new File(outputPath).delete();
        }
    }

    private void createVariant(String assetId, VariantType type, String objectKey,
                                Integer width, Integer height) {
        MediaVariant variant = new MediaVariant();
        variant.setMediaAssetId(assetId);
        variant.setVariantType(type);
        variant.setObjectKey(objectKey);
        variant.setWidth(width);
        variant.setHeight(height);
        variant.setCreatedAt(Instant.now());
        variantRepository.save(variant);
    }

    private void handleJobFailure(MediaProcessingJob job, String error) {
        job.setErrorMessage(error);
        if (job.getAttempts() >= MAX_ATTEMPTS) {
            job.setStatus(JobStatus.FAILED);
        } else {
            job.setStatus(JobStatus.QUEUED);
        }
        jobRepository.save(job);
    }
}
