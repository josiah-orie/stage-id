package com.jovine360.talenthub.sport.api;

import com.jovine360.talenthub.shared.domain.service.AuditTrail;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import com.jovine360.talenthub.sport.api.SportAdminDtos.*;
import com.jovine360.talenthub.sport.application.service.SportAdministrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Sport configuration, reserved for SUPER_ADMIN. */
@RestController
@RequestMapping("/api/v1/admin/sports")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Super admin: sports", description = "Sports, disciplines and metric definitions")
public class AdminSportController {

    private final SportAdministrationService sportAdministration;
    private final AuditTrail audit;

    public AdminSportController(SportAdministrationService sportAdministration, AuditTrail audit) {
        this.sportAdministration = sportAdministration;
        this.audit = audit;
    }

    @Operation(summary = "Create a sport")
    @PostMapping
    public ResponseEntity<SportDto> create(@Valid @RequestBody CreateSportRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        SportDto created = sportAdministration.createSport(request);
        audit.record(principal.userId(), principal.roles(), "SPORT_CREATED", "Sport", created.id(), created.code());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Add a discipline to a sport")
    @PostMapping("/{id}/disciplines")
    public ResponseEntity<DisciplineDto> addDiscipline(@PathVariable String id,
            @Valid @RequestBody CreateDisciplineRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        DisciplineDto created = sportAdministration.addDiscipline(id, request);
        audit.record(principal.userId(), principal.roles(), "SPORT_DISCIPLINE_ADDED", "Sport", id, created.code());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Add a performance metric definition to a sport")
    @PostMapping("/{id}/metrics")
    public ResponseEntity<MetricDto> addMetric(@PathVariable String id,
            @Valid @RequestBody CreateMetricRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        MetricDto created = sportAdministration.addMetric(id, request);
        audit.record(principal.userId(), principal.roles(), "SPORT_METRIC_ADDED", "Sport", id, created.code());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
