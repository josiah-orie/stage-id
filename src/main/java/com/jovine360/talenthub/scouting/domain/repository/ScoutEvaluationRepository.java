package com.jovine360.talenthub.scouting.domain.repository;

import com.jovine360.talenthub.scouting.domain.model.ScoutEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoutEvaluationRepository extends JpaRepository<ScoutEvaluation, String> {
    List<ScoutEvaluation> findByScoutProfileId(String scoutProfileId);
    List<ScoutEvaluation> findByAthleteId(String athleteId);
}
