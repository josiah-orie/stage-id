package com.jovine360.talenthub.activity.domain.repository;

import com.jovine360.talenthub.activity.domain.model.AthleteActivityPerformance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AthleteActivityPerformanceRepository extends JpaRepository<AthleteActivityPerformance, String> {
    List<AthleteActivityPerformance> findByActivityId(String activityId);
}
