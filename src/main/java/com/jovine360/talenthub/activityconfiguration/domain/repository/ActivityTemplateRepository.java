package com.jovine360.talenthub.activityconfiguration.domain.repository;

import com.jovine360.talenthub.activityconfiguration.domain.model.ActivityTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityTemplateRepository extends JpaRepository<ActivityTemplate, String> {
    Optional<ActivityTemplate> findByCode(String code);
    List<ActivityTemplate> findByActiveTrue();
    List<ActivityTemplate> findBySportCodeAndActiveTrue(String sportCode);
}
