package com.jovine360.talenthub.sport.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request and response types for sport configuration. */
public final class SportAdminDtos {

    private SportAdminDtos() {}

    public record CreateSportRequest(
            @NotBlank @Size(max = 10) @Pattern(regexp = "[A-Z0-9_]+") String code,
            @NotBlank @Size(max = 100) String name,
            String description) {}

    public record CreateDisciplineRequest(
            @NotBlank @Size(max = 30) @Pattern(regexp = "[A-Z0-9_]+") String code,
            @NotBlank @Size(max = 100) String name) {}

    public record CreateMetricRequest(
            @NotBlank @Size(max = 50) @Pattern(regexp = "[A-Z0-9_]+") String code,
            @NotBlank @Size(max = 100) String name,
            @Size(max = 20) String unit,
            Boolean higherIsBetter) {}

    public record DisciplineDto(String id, String sportId, String code, String name, boolean active) {}

    public record MetricDto(String id, String sportId, String code, String name, String unit,
                            boolean higherIsBetter, boolean active) {}
}
