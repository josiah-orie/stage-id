package com.jovine360.talenthub.safeguarding.domain.repository;

import com.jovine360.talenthub.safeguarding.domain.model.SafeguardingReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SafeguardingReportRepository extends JpaRepository<SafeguardingReport, String> {
    List<SafeguardingReport> findByReporterId(String reporterId);
    List<SafeguardingReport> findByStatus(String status);
}
