package com.jovine360.talenthub.athlete.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddAthleteSportRequest(@NotBlank String sportCode, @Size(max = 100) String position) {}
