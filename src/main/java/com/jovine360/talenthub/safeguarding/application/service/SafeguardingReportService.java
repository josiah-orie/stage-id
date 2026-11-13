package com.jovine360.talenthub.safeguarding.application.service;

import com.jovine360.talenthub.safeguarding.domain.model.SafeguardingReport;
import com.jovine360.talenthub.safeguarding.domain.repository.SafeguardingReportRepository;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class SafeguardingReportService {

    private final SafeguardingReportRepository reportRepository;

    public SafeguardingReportService(SafeguardingReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public SafeguardingReport submitReport(String reporterId, String reportedEntityId,
                                            String entityType, String reportType, String description) {
        SafeguardingReport report = new SafeguardingReport();
        report.setReporterId(reporterId);
        report.setReportedEntityId(reportedEntityId);
        report.setReportedEntityType(entityType);
        report.setReportType(reportType);
        report.setDescription(description);
        report.setStatus("PENDING");
        report.setSubmittedAt(Instant.now());
        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<SafeguardingReport> getMyReports(String reporterId) {
        return reportRepository.findByReporterId(reporterId);
    }

    public SafeguardingReport reviewReport(String reportId, String status, String resolvedBy) {
        SafeguardingReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("SafeguardingReport", reportId));
        report.setStatus(status);
        report.setResolvedBy(resolvedBy);
        report.setResolvedAt(Instant.now());
        return reportRepository.save(report);
    }
}
