package com.jovine360.talenthub.scouting.infrastructure.web;

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

@RestController
@RequestMapping("/api/v1/scouting")
@PreAuthorize("hasRole('SCOUT')")
public class ScoutingController {

    private final WatchlistService watchlistService;
    private final ScoutEvaluationService evaluationService;

    public ScoutingController(WatchlistService watchlistService,
                               ScoutEvaluationService evaluationService) {
        this.watchlistService = watchlistService;
        this.evaluationService = evaluationService;
    }

    // ---- Favourites ----
    @PostMapping("/favourites")
    public ResponseEntity<ScoutFavourite> addFavourite(
            @RequestParam String scoutProfileId, @RequestParam String athleteId,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(watchlistService.addFavourite(scoutProfileId, athleteId, notes));
    }

    @DeleteMapping("/favourites")
    public ResponseEntity<Void> removeFavourite(
            @RequestParam String scoutProfileId, @RequestParam String athleteId) {
        watchlistService.removeFavourite(scoutProfileId, athleteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favourites")
    public ResponseEntity<List<ScoutFavourite>> getFavourites(@RequestParam String scoutProfileId) {
        return ResponseEntity.ok(watchlistService.getFavourites(scoutProfileId));
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

    @GetMapping("/watchlists/{id}")
    public ResponseEntity<Watchlist> getWatchlist(@PathVariable String id) {
        return ResponseEntity.ok(watchlistService.getWatchlist(id));
    }

    @PostMapping("/watchlists/{id}/athletes")
    public ResponseEntity<WatchlistAthlete> addToWatchlist(
            @PathVariable String id, @RequestParam String athleteId,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(watchlistService.addToWatchlist(id, athleteId, principal.userId(), notes));
    }

    @DeleteMapping("/watchlists/{id}/athletes/{athleteId}")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable String id, @PathVariable String athleteId) {
        watchlistService.removeFromWatchlist(id, athleteId);
        return ResponseEntity.noContent().build();
    }

    // ---- Evaluations ----
    record CreateEvaluationRequest(@NotBlank String scoutProfileId, @NotBlank String athleteId,
                                    String sportId, @NotBlank String title, LocalDate evaluationDate) {}

    @PostMapping("/evaluations")
    public ResponseEntity<ScoutEvaluation> createEvaluation(@Valid @RequestBody CreateEvaluationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evaluationService.createEvaluation(req.scoutProfileId(), req.athleteId(),
                        req.sportId(), req.title(), req.evaluationDate()));
    }

    @PostMapping("/evaluations/{id}/complete")
    public ResponseEntity<ScoutEvaluation> completeEvaluation(@PathVariable String id) {
        return ResponseEntity.ok(evaluationService.completeEvaluation(id));
    }

    @GetMapping("/evaluations")
    public ResponseEntity<List<ScoutEvaluation>> getMyEvaluations(@RequestParam String scoutProfileId) {
        return ResponseEntity.ok(evaluationService.getMyEvaluations(scoutProfileId));
    }
}
