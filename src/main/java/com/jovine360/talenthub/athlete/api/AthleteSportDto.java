package com.jovine360.talenthub.athlete.api;

public record AthleteSportDto(String id, String athleteUserId, String sportId, String sportCode,
                              String position, boolean primary) {}
