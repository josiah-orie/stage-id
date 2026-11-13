package com.jovine360.talenthub.development.domain.repository;

import com.jovine360.talenthub.development.domain.model.DevelopmentMilestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DevelopmentMilestoneRepository extends JpaRepository<DevelopmentMilestone, String> {
    List<DevelopmentMilestone> findByAthleteIdOrderByMilestoneDateDesc(String athleteId);
}
