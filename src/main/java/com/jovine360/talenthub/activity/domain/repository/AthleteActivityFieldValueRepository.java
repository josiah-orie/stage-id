package com.jovine360.talenthub.activity.domain.repository;

import com.jovine360.talenthub.activity.domain.model.AthleteActivityFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AthleteActivityFieldValueRepository extends JpaRepository<AthleteActivityFieldValue, String> {
    List<AthleteActivityFieldValue> findByActivityId(String activityId);
}
