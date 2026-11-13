package com.jovine360.talenthub.verification.domain.event;

public record AthleteActivityVerifiedEvent(
        String activityId,
        String athleteId,
        String verifierId
) {}
