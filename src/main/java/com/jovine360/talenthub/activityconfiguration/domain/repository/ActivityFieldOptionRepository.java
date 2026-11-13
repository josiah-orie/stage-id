package com.jovine360.talenthub.activityconfiguration.domain.repository;

import com.jovine360.talenthub.activityconfiguration.domain.model.ActivityFieldOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityFieldOptionRepository extends JpaRepository<ActivityFieldOption, String> {
    List<ActivityFieldOption> findByFieldDefinitionIdOrderByOrdinal(String fieldDefinitionId);
}
