package com.jovine360.talenthub.activity.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LogActivityRequest(
        @NotBlank String templateCode,
        @NotNull  LocalDate sessionDate,
        String sportCode,
        @NotBlank String dataJson,
        String coachNotes
) {}
