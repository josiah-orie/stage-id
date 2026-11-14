package com.jovine360.talenthub.scout.domain.repository;

import com.jovine360.talenthub.scout.domain.model.ScoutSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoutSpecializationRepository extends JpaRepository<ScoutSpecialization, String> {
    List<ScoutSpecialization> findByScoutProfileId(String scoutProfileId);
    boolean existsByScoutProfileIdAndSportId(String scoutProfileId, String sportId);
}
