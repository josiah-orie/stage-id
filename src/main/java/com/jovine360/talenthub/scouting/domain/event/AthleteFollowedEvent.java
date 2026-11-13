package com.jovine360.talenthub.scouting.domain.event;

public record AthleteFollowedEvent(
        String followerId,
        String followerType,
        String athleteId
) {}
