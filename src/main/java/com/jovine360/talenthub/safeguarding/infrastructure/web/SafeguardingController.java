package com.jovine360.talenthub.safeguarding.infrastructure.web;

import com.jovine360.talenthub.athleteaccess.api.AthleteAccessPolicy;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.guardian.api.GuardianApi;
import com.jovine360.talenthub.safeguarding.application.service.ConsentService;
import com.jovine360.talenthub.safeguarding.application.service.SafeguardingReportService;
import com.jovine360.talenthub.safeguarding.domain.model.ConsentPurpose;
import com.jovine360.talenthub.safeguarding.domain.model.ConsentRecord;
import com.jovine360.talenthub.safeguarding.domain.model.SafeguardingReport;
import com.jovine360.talenthub.shared.domain.service.AuditTrail;
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
    private final AthleteAccessPolicy accessPolicy;
    private final GuardianApi guardianApi;
    private final AuditTrail audit;
    private final com.jovine360.talenthub.safeguarding.application.service.DataSubjectRequestService dsrService;

    public SafeguardingController(ConsentService consentService,
                                   SafeguardingReportService reportService,
                                   AthleteAccessPolicy accessPolicy,
                                   GuardianApi guardianApi,
                                   AuditTrail audit,
                                   com.jovine360.talenthub.safeguarding.application.service.DataSubjectRequestService dsrService) {
        this.consentService = consentService;
        this.reportService = reportService;
        this.accessPolicy = accessPolicy;
        this.guardianApi = guardianApi;
        this.audit = audit;
        this.dsrService = dsrService;
    }

    record ConsentRequest(@NotNull ConsentPurpose purpose, String consentVersion,
                           String authorizationSource) {}

    @PostMapping("/athletes/{id}/consent")
    public ResponseEntity<ConsentRecord> recordConsent(
            @PathVariable String id, @Valid @RequestBody ConsentRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        accessPolicy.assertCanAccess(id, principal.userId(), principal.roles(), AthletePermission.MANAGE);
        // A minor cannot become discoverable unless an active guardian link exists
        if (req.purpose() == ConsentPurpose.PROFILE_DISCOVERY) {
            guardianApi.assertGuardianConsentForMinor(id);
        }
        ConsentRecord record = consentService.recordConsent(id, req.purpose(),
                req.consentVersion(), principal.userId(), req.authorizationSource());
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @GetMapping("/athletes/{id}/consent")
    public ResponseEntity<List<ConsentRecord>> listConsent(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        accessPolicy.assertCanAccess(id, principal.userId(), principal.roles(), AthletePermission.VIEW_PROFILE);
        return ResponseEntity.ok(consentService.listConsents(id));
    }

    @DeleteMapping("/athletes/{id}/consent/{purpose}")
    public ResponseEntity<Void> revokeConsent(
            @PathVariable String id, @PathVariable ConsentPurpose purpose,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        accessPolicy.assertCanAccess(id, principal.userId(), principal.roles(), AthletePermission.MANAGE);
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

    // ---- data subject requests ----

    record DataSubjectRequestBody(@NotBlank String requestType) {}

    @PostMapping("/safeguarding/data-subject-requests")
    public ResponseEntity<com.jovine360.talenthub.safeguarding.domain.model.DataSubjectRequest> submitDsr(
            @Valid @RequestBody DataSubjectRequestBody body,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dsrService.submit(principal.userId(), body.requestType()));
    }

    @GetMapping("/safeguarding/data-subject-requests")
    public ResponseEntity<List<com.jovine360.talenthub.safeguarding.domain.model.DataSubjectRequest>> myDsrs(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(dsrService.mine(principal.userId()));
    }

    @GetMapping("/admin/data-subject-requests")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<com.jovine360.talenthub.safeguarding.domain.model.DataSubjectRequest>> listDsrs(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(dsrService.list(status));
    }

    @PostMapping("/admin/data-subject-requests/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<com.jovine360.talenthub.safeguarding.domain.model.DataSubjectRequest> resolveDsr(
            @PathVariable String id, @RequestParam String outcome,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        var result = dsrService.resolve(id, outcome);
        audit.record(principal.userId(), principal.roles(), "DATA_SUBJECT_REQUEST_RESOLVED",
                "DataSubjectRequest", id, result.getStatus());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/safeguarding/reports")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<SafeguardingReport>> listReports(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(reportService.listReports(status));
    }

    @PostMapping("/admin/safeguarding/reports/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<SafeguardingReport> reviewReport(
            @PathVariable String id, @RequestParam String status,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        SafeguardingReport result = reportService.reviewReport(id, status, principal.userId());
        audit.record(principal.userId(), principal.roles(), "SAFEGUARDING_REPORT_REVIEWED", "SafeguardingReport", id, status);
        return ResponseEntity.ok(result);
    }
}
