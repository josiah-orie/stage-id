package com.jovine360.talenthub.development.api;

import java.time.LocalDate;

public record MilestoneDto(String id, String athleteId, String sportId, String milestoneType, String title,
                           String description, LocalDate milestoneDate, String evidenceActivityId,
                           String createdBy) {}
