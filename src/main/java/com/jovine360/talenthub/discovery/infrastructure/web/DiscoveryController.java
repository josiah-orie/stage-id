package com.jovine360.talenthub.discovery.infrastructure.web;

import com.jovine360.talenthub.discovery.application.service.AthleteDiscoveryService;
import com.jovine360.talenthub.discovery.domain.model.AthleteSearchProjection;
import com.jovine360.talenthub.discovery.domain.model.SavedSearch;
import com.jovine360.talenthub.scout.api.ScoutApi;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discovery")
@PreAuthorize("hasRole('SCOUT')")
public class DiscoveryController {

    private final AthleteDiscoveryService discoveryService;
    private final ScoutApi scoutApi;

    public DiscoveryController(AthleteDiscoveryService discoveryService, ScoutApi scoutApi) {
        this.discoveryService = discoveryService;
        this.scoutApi = scoutApi;
    }

    @GetMapping("/athletes")
    public ResponseEntity<PageResponse<AthleteSearchProjection>> searchAthletes(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String ageCategory,
            Pageable pageable) {
        return ResponseEntity.ok(discoveryService.searchAthletes(country, ageCategory, pageable));
    }

    @GetMapping("/athletes/{id}")
    public ResponseEntity<AthleteSearchProjection> getProfile(@PathVariable String id) {
        return ResponseEntity.ok(discoveryService.getDiscoveryProfile(id));
    }

    record SaveSearchRequest(@NotBlank String name, String queryJson) {}

    /** The search is saved against the caller's own scout profile. */
    @PostMapping("/saved-searches")
    public ResponseEntity<SavedSearch> saveSearch(
            @Valid @RequestBody SaveSearchRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        String scoutProfileId = scoutApi.requireScoutProfileIdForUser(principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(discoveryService.saveSearch(scoutProfileId, req.name(), req.queryJson()));
    }

    @GetMapping("/saved-searches")
    public ResponseEntity<List<SavedSearch>> getSavedSearches(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        String scoutProfileId = scoutApi.requireScoutProfileIdForUser(principal.userId());
        return ResponseEntity.ok(discoveryService.getSavedSearches(scoutProfileId));
    }
}
