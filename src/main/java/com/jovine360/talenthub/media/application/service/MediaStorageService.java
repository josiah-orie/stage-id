package com.jovine360.talenthub.media.application.service;

import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Low-level MinIO storage operations.
 */
@Service
public class MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(MediaStorageService.class);
    private static final int PRESIGNED_EXPIRY_MINUTES = 15;

    private final MinioClient minioClient;

    public MediaStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String generatePresignedUploadUrl(String bucketName, String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry(PRESIGNED_EXPIRY_MINUTES, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate upload URL", e);
        }
    }

    public String generatePresignedDownloadUrl(String bucketName, String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry(PRESIGNED_EXPIRY_MINUTES, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }

    public boolean objectExists(String bucketName, String objectKey) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName).object(objectKey).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void softDelete(String bucketName, String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName).object(objectKey).build());
        } catch (Exception e) {
            log.warn("Could not delete object {}/{}: {}", bucketName, objectKey, e.getMessage());
        }
    }
}
