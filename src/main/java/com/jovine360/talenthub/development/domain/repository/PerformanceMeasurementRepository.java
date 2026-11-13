package com.jovine360.talenthub.development.domain.repository;

import com.jovine360.talenthub.development.domain.model.PerformanceMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PerformanceMeasurementRepository extends JpaRepository<PerformanceMeasurement, String> {

    /** Excludes measurements that have been corrected (i.e., a newer row references them). */
    @Query("SELECT m FROM PerformanceMeasurement m WHERE m.athleteId = :athleteId " +
           "AND m.correctedFromId IS NULL ORDER BY m.observedAt DESC")
    List<PerformanceMeasurement> findActiveByAthleteId(String athleteId);

    @Query("SELECT m FROM PerformanceMeasurement m WHERE m.athleteId = :athleteId " +
           "AND m.metricCode = :metricCode AND m.correctedFromId IS NULL ORDER BY m.observedAt ASC")
    List<PerformanceMeasurement> findChartData(String athleteId, String metricCode);

    List<PerformanceMeasurement> findBySourceActivityId(String sourceActivityId);
}
