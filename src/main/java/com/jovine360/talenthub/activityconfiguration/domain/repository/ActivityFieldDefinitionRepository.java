package com.jovine360.talenthub.activityconfiguration.domain.repository;

import com.jovine360.talenthub.activityconfiguration.domain.model.ActivityFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityFieldDefinitionRepository extends JpaRepository<ActivityFieldDefinition, String> {
    List<ActivityFieldDefinition> findBySchemaVersionIdAndActiveTrueOrderByDisplayOrder(String schemaVersionId);
}
