package com.jovine360.talenthub.activity.api;

import java.time.LocalDate;

public record ActivitySessionDto(
        String id, String athleteUserId, String templateCode,
        LocalDate sessionDate, String sportCode, String dataJson,
        String coachNotes, String loggedBy
) {}
