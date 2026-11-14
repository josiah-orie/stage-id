package com.jovine360.talenthub.scouting;

import com.jovine360.talenthub.scouting.application.service.ScoutEvaluationService;
import com.jovine360.talenthub.scouting.application.service.ScoutEvaluationService.ScoreInput;
import com.jovine360.talenthub.scouting.application.service.ScoutNoteService;
import com.jovine360.talenthub.scouting.application.service.WatchlistService;
import com.jovine360.talenthub.scouting.domain.model.*;
import com.jovine360.talenthub.scouting.domain.repository.*;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import com.jovine360.talenthub.shared.domain.service.EntitlementService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Favourites/follows idempotency, notes ownership and evaluation scoring. No Spring context. */
class ScoutFeaturesTest {

    @Test
    void duplicateFavouriteIsConflictAndFollowIsIdempotent() {
        ScoutFavouriteRepository favs = mock(ScoutFavouriteRepository.class);
        AthleteFollowRepository follows = mock(AthleteFollowRepository.class);
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        WatchlistService service = new WatchlistService(favs, mock(WatchlistRepository.class),
                mock(WatchlistAthleteRepository.class), follows, mock(EntitlementService.class), events);

        when(favs.findByScoutProfileIdAndAthleteId("sp-1", "a-1")).thenReturn(Optional.of(new ScoutFavourite()));
        assertThatThrownBy(() -> service.addFavourite("sp-1", "a-1", null)).isInstanceOf(DuplicateResourceException.class);

        AthleteFollow existing = new AthleteFollow();
        when(follows.findFirstByFollowerIdAndAthleteId("u-1", "a-1")).thenReturn(Optional.of(existing));
        assertThat(service.followAthlete("u-1", OwnerType.SCOUT, "a-1")).isSameAs(existing);
        verify(follows, never()).save(any());
        verifyNoInteractions(events);

        when(follows.findByFollowerIdAndAthleteId("u-1", "a-9")).thenReturn(List.of());
        assertThatThrownBy(() -> service.unfollow("u-1", "a-9")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void notesAreOnlyDeletableByTheirAuthor() {
        ScoutNoteRepository repo = mock(ScoutNoteRepository.class);
        ScoutNote note = new ScoutNote();
        note.setId("n-1");
        note.setScoutProfileId("sp-1");
        when(repo.findById("n-1")).thenReturn(Optional.of(note));
        ScoutNoteService service = new ScoutNoteService(repo);

        assertThatThrownBy(() -> service.delete("n-1", "sp-2")).isInstanceOf(EntityNotFoundException.class);
        service.delete("n-1", "sp-1");
        verify(repo).delete(note);
    }

    @Test
    void scoresAreValidatedAveragedAndOnlyOnOwnDraft() {
        ScoutEvaluationRepository evals = mock(ScoutEvaluationRepository.class);
        EvaluationScoreRepository scores = mock(EvaluationScoreRepository.class);
        EvaluationCriterionRepository criteria = mock(EvaluationCriterionRepository.class);
        ScoutEvaluation e = new ScoutEvaluation();
        e.setId("ev-1");
        e.setScoutProfileId("sp-1");
        e.setStatus(EvaluationStatus.DRAFT);
        when(evals.findById("ev-1")).thenReturn(Optional.of(e));
        EvaluationCriterion tech = new EvaluationCriterion();
        tech.setId("c-1");
        tech.setCriterionCode("FB_TECHNICAL");
        tech.setCriterionName("Technical");
        when(criteria.findByCriterionCodeAndActiveTrue("FB_TECHNICAL")).thenReturn(Optional.of(tech));
        when(criteria.findById("c-1")).thenReturn(Optional.of(tech));
        when(scores.findByEvaluationIdAndCriterionId("ev-1", "c-1")).thenReturn(Optional.empty());
        EvaluationScore saved = new EvaluationScore();
        saved.setCriterionId("c-1");
        saved.setScore(new BigDecimal("8"));
        when(scores.findByEvaluationId("ev-1")).thenReturn(List.of(saved));
        ScoutEvaluationService service = new ScoutEvaluationService(evals, mock(EntitlementService.class), scores, criteria);

        var detail = service.addScores("ev-1", "sp-1", List.of(new ScoreInput("FB_TECHNICAL", new BigDecimal("8"), null)));
        assertThat(detail.averageScore()).isEqualByComparingTo("8.0");
        assertThat(detail.scores().get(0).criterionCode()).isEqualTo("FB_TECHNICAL");

        assertThatThrownBy(() -> service.addScores("ev-1", "sp-1", List.of(new ScoreInput("FB_TECHNICAL", new BigDecimal("11"), null))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.addScores("ev-1", "sp-2", List.of(new ScoreInput("FB_TECHNICAL", BigDecimal.ONE, null))))
                .isInstanceOf(SecurityException.class);

        e.setStatus(EvaluationStatus.COMPLETED);
        assertThatThrownBy(() -> service.addScores("ev-1", "sp-1", List.of(new ScoreInput("FB_TECHNICAL", BigDecimal.ONE, null))))
                .isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(() -> service.completeEvaluation("ev-1", "sp-2")).isInstanceOf(SecurityException.class);
    }
}
