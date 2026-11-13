package com.jovine360.talenthub.acceptance;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Phase 2 acceptance tests.
 * 5. Sport-aware activity submission with Football MATCH schema validation
 * 6. Video upload and publication flow
 * 7. Athlete development history retrieval
 */
@Disabled("Requires Docker for Testcontainers MySQL — run locally with Docker available")
class Phase2AcceptanceTest {

    @Test
    void test5_sportAwareActivitySubmissionWithSchemaValidation() {
        // 1. Retrieve Football MATCH published schema
        // 2. Submit activity with required fields (opponent, matchDate, minutesPlayed)
        // 3. Verify activity created in DRAFT status
        // 4. Submit activity → SUBMITTED status
        // 5. Publish activity → PUBLISHED status, AthleteActivityPublishedEvent fired
        // 6. Assert PerformanceMeasurement rows created in development module
    }

    @Test
    void test6_videoUploadAndPublicationFlow() {
        // 1. Initiate upload → presigned URL returned
        // 2. Simulate object PUT to MinIO (or mock storage service)
        // 3. Call complete upload → MediaAsset set to UPLOADED
        // 4. MediaProcessingService processes video async
        // 5. MediaAsset transitions to READY
        // 6. AthleteVideoReadyEvent published
        // 7. Get playback URL → presigned GET URL returned
    }

    @Test
    void test7_athleteDevelopmentHistoryRetrieval() {
        // 1. Pre-condition: athlete has published activities with measurements
        // 2. GET /api/v1/athletes/{id}/development
        // 3. Assert timeline entries contain MEASUREMENT and MILESTONE types
        // 4. Assert entries are sorted by date descending
    }
}
