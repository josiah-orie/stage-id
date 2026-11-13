package com.jovine360.talenthub.administration.api;

public record PlatformDashboardResponse(
        long totalAthletes,
        long verifiedAthletes,
        long registeredScouts,
        long verifiedScouts,
        long registeredOrgs,
        long activeSubscriptions,
        long publishedActivities,
        long uploadedVideos,
        long pendingVerifications,
        long pendingModerationReports
) {}
