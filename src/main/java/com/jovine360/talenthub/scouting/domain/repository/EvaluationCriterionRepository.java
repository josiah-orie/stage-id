package com.jovine360.talenthub.scouting.domain.repository;

import com.jovine360.talenthub.scouting.domain.model.EvaluationCriterion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluationCriterionRepository extends JpaRepository<EvaluationCriterion, String> {
    List<EvaluationCriterion> findByActiveTrueOrderByCategoryAscDisplayOrderAsc();
    List<EvaluationCriterion> findByCategoryIgnoreCaseAndActiveTrueOrderByDisplayOrderAsc(String category);
    Optional<EvaluationCriterion> findByCriterionCodeAndActiveTrue(String criterionCode);
}
