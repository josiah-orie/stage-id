package com.jovine360.talenthub.scouting.infrastructure.web;

import com.jovine360.talenthub.scout.api.ScoutApi;
import com.jovine360.talenthub.scouting.application.service.ScoutEvaluationService;
import com.jovine360.talenthub.scouting.application.service.WatchlistService;
import com.jovine360.talenthub.scouting.domain.model.*;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * A scout's own favourites, watchlists and evaluations. The scout profile is always taken from
 * the caller's token; a scout can never read or change another scout's data.
 */
@RestController
@RequestMapping("/api/v1/scouting")
@PreAuthorize("hasRole('SCOUT')")
public class ScoutingController {

    private final WatchlistService watchlistService;
    private final ScoutEvaluationService evaluationService;
    private final ScoutApi scoutApi;
    private final com.jovine360.talenthub.scouting.application.service.ScoutNoteService noteService;

    public ScoutingController(WatchlistService watchlistService,
                              ScoutEvaluationService evaluationService,
                              ScoutApi scoutApi,
                              com.jovine360.talenthub.scouting.application.service.ScoutNoteService noteService) {
        this.watchlistService = watchlistService;
        this.evaluationService = evaluationService;
        this.scoutApi = scoutApi;
        this.noteService = noteService;
    }

    private String scoutProfileId(AuthenticatedPrincipal principal) {
        return scoutApi.requireScoutProfileIdForUser(principal.userId());
    }

    // ---- Favourites ----

    @PostMapping("/favourites")
    public ResponseEntity<ScoutFavourite> addFavourite(
            @RequestParam String athleteId,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(watchlistService.addFavourite(scoutProfileId(principal), athleteId, notes));
    }

    @DeleteMapping("/favourites")
    public ResponseEntity<Void> removeFavourite(
            @RequestParam String athleteId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        watchlistService.removeFavourite(scoutProfileId(principal), athleteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favourites")
    public ResponseEntity<List<ScoutFavourite>> getFavourites(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(watchlistService.getFavourites(scoutProfileId(principal)));
    }

    // ---- Follows ----

    record FollowRequest(@NotBlank String athleteId) {}

    @PostMapping("/follows")
    public ResponseEntity<AthleteFollow> follow(@Valid @RequestBody FollowRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(watchlistService.followAthlete(principal.userId(), OwnerType.SCOUT, req.athleteId()));
    }

    @GetMapping("/follows")
    public ResponseEntity<List<AthleteFollow>> follows(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(watchlistService.getFollows(principal.userId()));
    }

    @DeleteMapping("/follows/{athleteId}")
    public ResponseEntity<Void> unfollow(@PathVariable String athleteId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        watchlistService.unfollow(principal.userId(), athleteId);
        return ResponseEntity.noContent().build();
    }

    // ---- Notes ----

    record NoteRequest(@NotBlank String athleteId, @NotBlank String noteText, LocalDate noteDate, Boolean isPrivate) {}

    @PostMapping("/notes")
    public ResponseEntity<ScoutNote> createNote(@Valid @RequestBody NoteRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.create(scoutProfileId(principal),
                req.athleteId(), req.noteText(), req.noteDate(), req.isPrivate()));
    }

    @GetMapping("/notes")
    public ResponseEntity<List<ScoutNote>> notes(@RequestParam(required = false) String athleteId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(noteService.list(scoutProfileId(principal), athleteId));
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        noteService.delete(id, scoutProfileId(principal));
        return ResponseEntity.noContent().build();
    }

    // ---- Watchlists ----

    record CreateWatchlistRequest(@NotBlank String name, String description, boolean shared) {}

    @PostMapping("/watchlists")
    public ResponseEntity<Watchlist> createWatchlist(
            @Valid @RequestBody CreateWatchlistRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(watchlistService.createWatchlist(principal.userId(), OwnerType.SCOUT,
                        req.name(), req.description(), req.shared()));
    }

    @GetMapping("/watchlists")
    public ResponseEntity<List<Watchlist>> getMyWatchlists(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(watchlistService.getWatchlistsOwnedBy(principal.userId()));
    }

    @GetMapping("/watchlists/{id}")
    public ResponseEntity<Watchlist> getWatchlist(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(watchlistService.getReadableWatchlist(id, principal.userId()));
    }

    @PostMapping("/watchlists/{id}/athletes")
    public ResponseEntity<WatchlistAthlete> addToWatchlist(
            @PathVariable String id, @RequestParam String athleteId,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        watchlistService.assertOwner(id, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(watchlistService.addToWatchlist(id, athleteId, principal.userId(), notes));
    }

    @DeleteMapping("/watchlists/{id}/athletes/{athleteId}")
    public ResponseEntity<Void> removeFromWatchlist(
            @PathVariable String id, @PathVariable String athleteId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        watchlistService.assertOwner(id, principal.userId());
        watchlistService.removeFromWatchlist(id, athleteId);
        return ResponseEntity.noContent().build();
    }

    // ---- Evaluations ----

    record CreateEvaluationRequest(@NotBlank String athleteId, String sportId,
                                   @NotBlank String title, LocalDate evaluationDate) {}

    @PostMapping("/evaluations")
    public ResponseEntity<ScoutEvaluation> createEvaluation(
            @Valid @RequestBody CreateEvaluationRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evaluationService.createEvaluation(scoutProfileId(principal), req.athleteId(),
                        req.sportId(), req.title(), req.evaluationDate()));
    }

    @GetMapping("/evaluation-criteria")
    public ResponseEntity<List<EvaluationCriterion>> criteria(@RequestParam(required = false) String category) {
        return ResponseEntity.ok(evaluationService.listCriteria(category));
    }

    @GetMapping("/evaluations/{id}")
    public ResponseEntity<com.jovine360.talenthub.scouting.application.service.EvaluationDetail> evaluation(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(evaluationService.getOwnDetail(id, scoutProfileId(principal)));
    }

    record ScoresRequest(@jakarta.validation.constraints.NotEmpty List<@Valid ScoreItem> scores) {}
    record ScoreItem(@NotBlank String criterionCode, @jakarta.validation.constraints.NotNull java.math.BigDecimal score,
                     String notes) {}

    @PostMapping("/evaluations/{id}/scores")
    public ResponseEntity<com.jovine360.talenthub.scouting.application.service.EvaluationDetail> addScores(
            @PathVariable String id, @Valid @RequestBody ScoresRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        var inputs = req.scores().stream()
                .map(x -> new ScoutEvaluationService.ScoreInput(x.criterionCode(), x.score(), x.notes())).toList();
        return ResponseEntity.ok(evaluationService.addScores(id, scoutProfileId(principal), inputs));
    }

    @PostMapping("/evaluations/{id}/complete")
    public ResponseEntity<ScoutEvaluation> completeEvaluation(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(evaluationService.completeEvaluation(id, scoutProfileId(principal)));
    }

    @GetMapping("/evaluations")
    public ResponseEntity<List<ScoutEvaluation>> getMyEvaluations(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(evaluationService.getMyEvaluations(scoutProfileId(principal)));
    }
}
