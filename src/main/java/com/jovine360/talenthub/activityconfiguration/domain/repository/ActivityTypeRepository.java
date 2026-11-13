package com.jovine360.talenthub.activityconfiguration.domain.repository;

import com.jovine360.talenthub.activityconfiguration.domain.model.ActivityType;
import com.jovine360.talenthub.activityconfiguration.domain.model.ActivityTypeCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivityTypeRepository extends JpaRepository<ActivityType, String> {
    Optional<ActivityType> findByCode(ActivityTypeCode code);
    java.util.List<ActivityType> findByActiveTrue();
}
