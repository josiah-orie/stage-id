package com.jovine360.talenthub.organization.api;

import com.jovine360.talenthub.organization.application.service.OrganizationService;
import com.jovine360.talenthub.organization.domain.model.OrgStatus;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.domain.service.AuditTrail;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "Organization verification", description = "Admin review of new organizations")
public class AdminOrganizationController {

    private final OrganizationService organizationService;
    private final AuditTrail audit;

    public AdminOrganizationController(OrganizationService organizationService, AuditTrail audit) {
        this.organizationService = organizationService;
        this.audit = audit;
    }

    record RejectRequest(@Size(max = 500) String reason) {}

    @Operation(summary = "Organizations by status (default PENDING_VERIFICATION)")
    @GetMapping("/verifications/organizations")
    public ResponseEntity<PageResponse<OrganizationDto>> pending(
            @RequestParam(defaultValue = "PENDING_VERIFICATION") OrgStatus status, Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(organizationService.listByStatus(status, pageable)));
    }

    @Operation(summary = "Approve a pending organization")
    @PostMapping("/organizations/{id}/verify")
    public ResponseEntity<OrganizationDto> verify(@PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        OrganizationDto result = organizationService.verify(id, principal.userId());
        audit.record(principal.userId(), principal.roles(), "ORGANIZATION_VERIFIED", "Organization", id, null);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Reject a pending organization")
    @PostMapping("/organizations/{id}/reject")
    public ResponseEntity<OrganizationDto> reject(@PathVariable String id,
            @RequestBody(required = false) RejectRequest body,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        String reason = body != null ? body.reason() : null;
        OrganizationDto result = organizationService.reject(id, principal.userId(), reason);
        audit.record(principal.userId(), principal.roles(), "ORGANIZATION_REJECTED", "Organization", id, reason);
        return ResponseEntity.ok(result);
    }
}
