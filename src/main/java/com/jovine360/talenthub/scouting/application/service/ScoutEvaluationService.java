package com.jovine360.talenthub.scouting.application.service;

import com.jovine360.talenthub.scouting.domain.model.*;
import com.jovine360.talenthub.scouting.domain.repository.*;
import com.jovine360.talenthub.shared.domain.exception.EntitlementLimitExceededException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import com.jovine360.talenthub.shared.domain.service.EntitlementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ScoutEvaluationService {

    private final ScoutEvaluationRepository evaluationRepository;
    private final EntitlementService entitlementService;
    private final EvaluationScoreRepository scoreRepository;
    private final EvaluationCriterionRepository criterionRepository;

    public ScoutEvaluationService(ScoutEvaluationRepository evaluationRepository,
                                   EntitlementService entitlementService,
                                   EvaluationScoreRepository scoreRepository,
                                   EvaluationCriterionRepository criterionRepository) {
        this.evaluationRepository = evaluationRepository;
        this.entitlementService = entitlementService;
        this.scoreRepository = scoreRepository;
        this.criterionRepository = criterionRepository;
    }

    public record ScoreInput(String criterionCode, BigDecimal score, String notes) {}

    @Transactional(readOnly = true)
    public List<EvaluationCriterion> listCriteria(String category) {
        return category == null || category.isBlank()
                ? criterionRepository.findByActiveTrueOrderByCategoryAscDisplayOrderAsc()
                : criterionRepository.findByCategoryIgnoreCaseAndActiveTrueOrderByDisplayOrderAsc(category);
    }

    /** Adds or replaces scores on the author's own DRAFT evaluation. Scores run from 0 to 10. */
    public EvaluationDetail addScores(String evaluationId, String scoutProfileId, List<ScoreInput> inputs) {
        ScoutEvaluation evaluation = loadOwned(evaluationId, scoutProfileId);
        if (evaluation.getStatus() != EvaluationStatus.DRAFT) {
            throw new InvalidStateTransitionException("ScoutEvaluation", evaluation.getStatus().name(), "SCORE");
        }
        for (ScoreInput in : inputs) {
            if (in.score() == null || in.score().compareTo(BigDecimal.ZERO) < 0
                    || in.score().compareTo(BigDecimal.TEN) > 0) {
                throw new IllegalArgumentException("score for " + in.criterionCode() + " must be between 0 and 10");
            }
            EvaluationCriterion criterion = criterionRepository.findByCriterionCodeAndActiveTrue(in.criterionCode())
                    .orElseThrow(() -> new EntityNotFoundException("EvaluationCriterion", in.criterionCode()));
            EvaluationScore score = scoreRepository.findByEvaluationIdAndCriterionId(evaluationId, criterion.getId())
                    .orElseGet(EvaluationScore::new);
            score.setEvaluationId(evaluationId);
            score.setCriterionId(criterion.getId());
            score.setScore(in.score());
            score.setNotes(in.notes());
            scoreRepository.save(score);
        }
        return detail(evaluation);
    }

    @Transactional(readOnly = true)
    public EvaluationDetail getOwnDetail(String evaluationId, String scoutProfileId) {
        return detail(loadOwned(evaluationId, scoutProfileId));
    }

    private ScoutEvaluation loadOwned(String evaluationId, String scoutProfileId) {
        ScoutEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new EntityNotFoundException("ScoutEvaluation", evaluationId));
        if (!evaluation.getScoutProfileId().equals(scoutProfileId)) {
            throw new SecurityException("Only the scout who created this evaluation can do this");
        }
        return evaluation;
    }

    private EvaluationDetail detail(ScoutEvaluation evaluation) {
        List<EvaluationDetail.Score> scores = scoreRepository.findByEvaluationId(evaluation.getId()).stream()
                .map(sc -> {
                    var c = criterionRepository.findById(sc.getCriterionId());
                    return new EvaluationDetail.Score(sc.getId(), sc.getCriterionId(),
                            c.map(EvaluationCriterion::getCriterionCode).orElse(null),
                            c.map(EvaluationCriterion::getCriterionName).orElse(null),
                            sc.getScore(), sc.getMaxScore(), sc.getNotes());
                }).toList();
        BigDecimal avg = scores.isEmpty() ? null : scores.stream()
                .map(EvaluationDetail.Score::score)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scores.size()), 1, java.math.RoundingMode.HALF_UP);
        return new EvaluationDetail(evaluation, scores, avg);
    }

    public ScoutEvaluation createEvaluation(String scoutProfileId, String athleteId,
                                             String sportId, String title, LocalDate evaluationDate) {
        // Feature gate: SCOUT_EVALUATIONS required
        if (!entitlementService.hasActivePlan(scoutProfileId)) {
            throw new EntitlementLimitExceededException("SCOUT_EVALUATIONS feature not available");
        }
        ScoutEvaluation evaluation = new ScoutEvaluation();
        evaluation.setScoutProfileId(scoutProfileId);
        evaluation.setAthleteId(athleteId);
        evaluation.setSportId(sportId);
        evaluation.setEvaluationTitle(title);
        evaluation.setEvaluationDate(evaluationDate);
        evaluation.setStatus(EvaluationStatus.DRAFT);
        return evaluationRepository.save(evaluation);
    }

    public ScoutEvaluation completeEvaluation(String evaluationId, String scoutProfileId) {
        ScoutEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new EntityNotFoundException("ScoutEvaluation", evaluationId));
        if (!evaluation.getScoutProfileId().equals(scoutProfileId)) {
            throw new SecurityException("Only the scout who created this evaluation can complete it");
        }
        if (evaluation.getStatus() != EvaluationStatus.DRAFT) {
            throw new InvalidStateTransitionException("ScoutEvaluation",
                    evaluation.getStatus().name(), "COMPLETED");
        }
        evaluation.setStatus(EvaluationStatus.COMPLETED);
        return evaluationRepository.save(evaluation);
    }

    @Transactional(readOnly = true)
    public List<ScoutEvaluation> getMyEvaluations(String scoutProfileId) {
        return evaluationRepository.findByScoutProfileId(scoutProfileId);
    }

    @Transactional(readOnly = true)
    public List<ScoutEvaluation> getEvaluationHistory(String athleteId) {
        return evaluationRepository.findByAthleteId(athleteId);
    }
}
