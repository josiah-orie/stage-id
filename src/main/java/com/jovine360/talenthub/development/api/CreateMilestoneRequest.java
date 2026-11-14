package com.jovine360.talenthub.development.api;

import com.jovine360.talenthub.development.domain.model.MilestoneType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * @param milestoneType defaults to ACHIEVEMENT
 * @param achievedAt    defaults to today
 */
public record CreateMilestoneRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        LocalDate achievedAt,
        MilestoneType milestoneType,
        String sportId,
        String evidenceActivityId) {}
