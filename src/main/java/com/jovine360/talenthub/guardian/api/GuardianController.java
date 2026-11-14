package com.jovine360.talenthub.guardian.api;

import com.jovine360.talenthub.guardian.application.service.GuardianService;
import com.jovine360.talenthub.guardian.domain.model.GuardianLinkStatus;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.domain.service.AuditTrail;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Guardian relationships", description = "Parent, legal guardian and agent links to an athlete")
public class GuardianController {

    private final GuardianService guardianService;
    private final AuditTrail audit;

    public GuardianController(GuardianService guardianService, AuditTrail audit) {
        this.guardianService = guardianService;
        this.audit = audit;
    }

    private void auditIfAdmin(AuthenticatedPrincipal p, String action, String linkId) {
        if (p.roles().contains("ADMIN") || p.roles().contains("SUPER_ADMIN")) {
            audit.record(p.userId(), p.roles(), action, "GuardianLink", linkId, null);
        }
    }

    @Operation(summary = "Request a PARENT, LEGAL_GUARDIAN or AGENT relationship with an athlete")
    @PostMapping("/guardian-relationships")
    public ResponseEntity<GuardianLinkDto> request(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateGuardianRelationshipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(guardianService.requestLink(principal.userId(), principal.roles(), request));
    }

    @Operation(summary = "List relationships the caller is part of, as requester or as athlete")
    @GetMapping("/guardian-relationships")
    public ResponseEntity<List<GuardianLinkDto>> listMine(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(guardianService.listMine(principal.userId()));
    }

    @Operation(summary = "Approve a relationship (athlete for PENDING, admin for PENDING_ADMIN_VERIFICATION)")
    @PostMapping("/guardian-relationships/{id}/approve")
    public ResponseEntity<GuardianLinkDto> approve(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable String id) {
        GuardianLinkDto result = guardianService.approve(id, principal.userId(), principal.roles());
        auditIfAdmin(principal, "GUARDIAN_LINK_APPROVED", id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Reject a pending relationship")
    @PostMapping("/guardian-relationships/{id}/reject")
    public ResponseEntity<GuardianLinkDto> reject(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable String id,
            @Valid @RequestBody(required = false) RejectRelationshipRequest body) {
        GuardianLinkDto result = guardianService.reject(id, principal.userId(), principal.roles(),
                body != null ? body.reason() : null);
        auditIfAdmin(principal, "GUARDIAN_LINK_REJECTED", id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Revoke a pending or active relationship (either party or an admin)")
    @PostMapping("/guardian-relationships/{id}/revoke")
    public ResponseEntity<GuardianLinkDto> revoke(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable String id) {
        return ResponseEntity.ok(guardianService.revoke(id, principal.userId(), principal.roles()));
    }

    @Operation(summary = "Athletes the caller manages as PARENT or LEGAL_GUARDIAN")
    @GetMapping("/guardians/me/athletes")
    public ResponseEntity<List<GuardianLinkDto>> myAthletes(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(guardianService.listGuardedAthletes(principal.userId()));
    }

    @Operation(summary = "Admin: list relationships by status (default PENDING_ADMIN_VERIFICATION)")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/admin/guardian-relationships")
    public ResponseEntity<PageResponse<GuardianLinkDto>> adminList(
            @RequestParam(defaultValue = "PENDING_ADMIN_VERIFICATION") GuardianLinkStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(guardianService.listByStatus(status, pageable)));
    }
}
