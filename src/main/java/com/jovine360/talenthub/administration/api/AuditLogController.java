package com.jovine360.talenthub.administration.api;

import com.jovine360.talenthub.administration.application.service.AuditLogService;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "Audit log", description = "Administrative action history")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Operation(summary = "Query the audit log. ADMIN sees only their own actions; SUPER_ADMIN sees all")
    @GetMapping
    public ResponseEntity<PageResponse<AuditLogDto>> search(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(auditLogService.search(principal.userId(), principal.roles(),
                actor, action, entityType, entityId, from, to, pageable)));
    }
}
