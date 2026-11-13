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

    public ScoutEvaluationService(ScoutEvaluationRepository evaluationRepository,
                                   EntitlementService entitlementService) {
        this.evaluationRepository = evaluationRepository;
        this.entitlementService = entitlementService;
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

    public ScoutEvaluation completeEvaluation(String evaluationId) {
        ScoutEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new EntityNotFoundException("ScoutEvaluation", evaluationId));
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
