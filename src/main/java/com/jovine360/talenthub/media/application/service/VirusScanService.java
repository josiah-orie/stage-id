package com.jovine360.talenthub.media.application.service;

/**
 * Interface for virus scanning of uploaded media.
 * The production implementation would integrate with a real AV scanner.
 */
public interface VirusScanService {
    boolean isSafe(String bucketName, String objectKey);
}
