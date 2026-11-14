package com.jovine360.talenthub.sport.domain.repository;

import com.jovine360.talenthub.sport.domain.model.SportMetricDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SportMetricDefinitionRepository extends JpaRepository<SportMetricDefinition, String> {
    List<SportMetricDefinition> findBySportIdOrderByName(String sportId);
    boolean existsBySportIdAndCode(String sportId, String code);
}
