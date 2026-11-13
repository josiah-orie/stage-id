package com.jovine360.talenthub.activity.domain.repository;

import com.jovine360.talenthub.activity.domain.model.AthleteActivity;
import com.jovine360.talenthub.activity.domain.model.ActivityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AthleteActivityRepository extends JpaRepository<AthleteActivity, String> {
    Page<AthleteActivity> findByAthleteId(String athleteId, Pageable pageable);
    Page<AthleteActivity> findByAthleteIdAndActivityStatus(String athleteId, ActivityStatus status, Pageable pageable);
}
