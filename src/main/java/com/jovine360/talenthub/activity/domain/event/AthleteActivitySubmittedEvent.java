package com.jovine360.talenthub.activity.domain.event;

import java.time.LocalDate;

public record AthleteActivitySubmittedEvent(
        String activityId,
        String athleteId,
        String activityTypeId,
        String sportId,
        LocalDate activityDate
) {}
