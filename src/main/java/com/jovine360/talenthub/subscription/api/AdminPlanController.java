package com.jovine360.talenthub.subscription.api;

import com.jovine360.talenthub.shared.domain.service.AuditTrail;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import com.jovine360.talenthub.subscription.api.PlanRequests.CreatePlanRequest;
import com.jovine360.talenthub.subscription.api.PlanRequests.UpdatePlanRequest;
import com.jovine360.talenthub.subscription.application.service.PlanAdministrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Subscription plan management, reserved for SUPER_ADMIN. */
@RestController
@RequestMapping("/api/v1/admin/subscription-plans")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Super admin: plans", description = "Create and change subscription plans")
public class AdminPlanController {

    private final PlanAdministrationService planService;
    private final AuditTrail audit;

    public AdminPlanController(PlanAdministrationService planService, AuditTrail audit) {
        this.planService = planService;
        this.audit = audit;
    }

    @Operation(summary = "List every plan, including inactive ones")
    @GetMapping
    public ResponseEntity<List<AdminPlanDto>> list() {
        return ResponseEntity.ok(planService.listAll());
    }

    @Operation(summary = "Get one plan")
    @GetMapping("/{id}")
    public ResponseEntity<AdminPlanDto> get(@PathVariable String id) {
        return ResponseEntity.ok(planService.get(id));
    }

    @Operation(summary = "Create a plan")
    @PostMapping
    public ResponseEntity<AdminPlanDto> create(@Valid @RequestBody CreatePlanRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        AdminPlanDto created = planService.create(request);
        audit.record(principal.userId(), principal.roles(), "PLAN_CREATED", "SubscriptionPlan", created.id(), created.code());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a plan (partial)")
    @PatchMapping("/{id}")
    public ResponseEntity<AdminPlanDto> update(@PathVariable String id, @Valid @RequestBody UpdatePlanRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        AdminPlanDto updated = planService.update(id, request);
        audit.record(principal.userId(), principal.roles(), "PLAN_UPDATED", "SubscriptionPlan", id, null);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Deactivate a plan. Existing subscriptions keep running")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<AdminPlanDto> deactivate(@PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        AdminPlanDto result = planService.deactivate(id);
        audit.record(principal.userId(), principal.roles(), "PLAN_DEACTIVATED", "SubscriptionPlan", id, null);
        return ResponseEntity.ok(result);
    }
}
