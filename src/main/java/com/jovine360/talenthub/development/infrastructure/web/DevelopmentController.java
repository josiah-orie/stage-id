package com.jovine360.talenthub.development.infrastructure.web;

import com.jovine360.talenthub.development.api.DevelopmentTimelineResponse;
import com.jovine360.talenthub.development.application.service.DevelopmentTimelineService;
import com.jovine360.talenthub.development.application.service.PerformanceMeasurementService;
import com.jovine360.talenthub.development.domain.model.MeasurementSource;
import com.jovine360.talenthub.development.domain.model.PerformanceMeasurement;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class DevelopmentController {

    private final DevelopmentTimelineService timelineService;
    private final PerformanceMeasurementService measurementService;

    public DevelopmentController(DevelopmentTimelineService timelineService,
                                  PerformanceMeasurementService measurementService) {
        this.timelineService = timelineService;
        this.measurementService = measurementService;
    }

    @GetMapping("/athletes/{id}/development")
    public ResponseEntity<DevelopmentTimelineResponse> getDevelopmentTimeline(@PathVariable String id) {
        return ResponseEntity.ok(timelineService.getTimeline(id));
    }

    @GetMapping("/athletes/{id}/performance")
    public ResponseEntity<List<PerformanceMeasurement>> getPerformanceChart(
            @PathVariable String id,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) String metric) {
        if (metric != null) {
            return ResponseEntity.ok(measurementService.getChartData(id, metric));
        }
        return ResponseEntity.ok(measurementService.getMeasurementHistory(id));
    }

    @GetMapping("/activities/{id}/measurements")
    public ResponseEntity<List<PerformanceMeasurement>> getActivityMeasurements(@PathVariable String id) {
        // Returns measurements sourced from a specific activity
        return ResponseEntity.ok(List.of());
    }

    record RecordMeasurementRequest(
            @NotBlank String metricCode,
            @NotNull BigDecimal measuredValue,
            String unit,
            LocalDate observedAt,
            String source
    ) {}

    @PostMapping("/athletes/{id}/measurements")
    public ResponseEntity<PerformanceMeasurement> recordMeasurement(
            @PathVariable String id,
            @Valid @RequestBody RecordMeasurementRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        MeasurementSource src = req.source() != null
                ? MeasurementSource.valueOf(req.source()) : MeasurementSource.SELF_REPORTED;
        PerformanceMeasurement measurement = measurementService.recordMeasurement(
                id, req.metricCode(), req.measuredValue(), req.unit(),
                req.observedAt() != null ? req.observedAt() : LocalDate.now(),
                principal.userId(), src);
        return ResponseEntity.status(HttpStatus.CREATED).body(measurement);
    }
}
