package com.jovine360.talenthub.safeguarding.infrastructure.web;

import com.jovine360.talenthub.safeguarding.application.service.ConsentService;
import com.jovine360.talenthub.safeguarding.application.service.SafeguardingReportService;
import com.jovine360.talenthub.safeguarding.domain.model.ConsentPurpose;
import com.jovine360.talenthub.safeguarding.domain.model.ConsentRecord;
import com.jovine360.talenthub.safeguarding.domain.model.SafeguardingReport;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SafeguardingController {

    private final ConsentService consentService;
    private final SafeguardingReportService reportService;

    public SafeguardingController(ConsentService consentService,
                                   SafeguardingReportService reportService) {
        this.consentService = consentService;
        this.reportService = reportService;
    }

    record ConsentRequest(@NotNull ConsentPurpose purpose, String consentVersion,
                           String authorizationSource) {}

    @PostMapping("/athletes/{id}/consent")
    public ResponseEntity<ConsentRecord> recordConsent(
            @PathVariable String id, @Valid @RequestBody ConsentRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        ConsentRecord record = consentService.recordConsent(id, req.purpose(),
                req.consentVersion(), principal.userId(), req.authorizationSource());
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @DeleteMapping("/athletes/{id}/consent/{purpose}")
    public ResponseEntity<Void> revokeConsent(
            @PathVariable String id, @PathVariable ConsentPurpose purpose,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        consentService.revokeConsent(id, purpose, principal.userId());
        return ResponseEntity.noContent().build();
    }

    record ReportRequest(@NotBlank String reportedEntityId, String entityType,
                          @NotBlank String reportType, String description) {}

    @PostMapping("/safeguarding/reports")
    public ResponseEntity<SafeguardingReport> submitReport(
            @Valid @RequestBody ReportRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        SafeguardingReport report = reportService.submitReport(
                principal.userId(), req.reportedEntityId(), req.entityType(),
                req.reportType(), req.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @GetMapping("/safeguarding/reports")
    public ResponseEntity<List<SafeguardingReport>> getMyReports(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(reportService.getMyReports(principal.userId()));
    }

    @PostMapping("/admin/safeguarding/reports/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<SafeguardingReport> reviewReport(
            @PathVariable String id, @RequestParam String status,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(reportService.reviewReport(id, status, principal.userId()));
    }
}
