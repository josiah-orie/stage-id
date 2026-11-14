package com.jovine360.talenthub.scouting.application.service;

import com.jovine360.talenthub.scouting.domain.model.ScoutEvaluation;

import java.math.BigDecimal;
import java.util.List;

/** An evaluation with its scores and the average score (null when there are none). */
public record EvaluationDetail(ScoutEvaluation evaluation, List<Score> scores, BigDecimal averageScore) {
    public record Score(String id, String criterionId, String criterionCode, String criterionName,
                        BigDecimal score, BigDecimal maxScore, String notes) {}
}
