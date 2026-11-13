package com.jovine360.talenthub.activityconfiguration.domain.repository;

import com.jovine360.talenthub.activityconfiguration.domain.model.ActivitySchemaVersion;
import com.jovine360.talenthub.activityconfiguration.domain.model.SchemaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivitySchemaVersionRepository extends JpaRepository<ActivitySchemaVersion, String> {
    Optional<ActivitySchemaVersion> findFirstByTemplateIdAndStatusOrderByVersionNumberDesc(
            String templateId, SchemaStatus status);
    List<ActivitySchemaVersion> findByTemplateIdOrderByVersionNumberDesc(String templateId);
    int countByTemplateId(String templateId);
}
