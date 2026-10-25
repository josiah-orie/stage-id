package com.jovine360.talenthub.guardian.api;

public record GuardianLinkDto(
        String id,
        String guardianUserId,
        String athleteUserId,
        String relationship,
        String status,
        boolean consentGiven
) {}
