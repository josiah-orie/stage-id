package com.jovine360.talenthub.media.application.service;

import org.springframework.stereotype.Service;

/**
 * Stub virus scan service — always returns safe.
 * Replace with ClamAV or similar in production.
 */
@Service
public class AlwaysPassVirusScanService implements VirusScanService {

    @Override
    public boolean isSafe(String bucketName, String objectKey) {
        return true;
    }
}
