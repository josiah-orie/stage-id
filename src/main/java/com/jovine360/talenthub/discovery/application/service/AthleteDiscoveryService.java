package com.jovine360.talenthub.discovery.application.service;

import com.jovine360.talenthub.activity.domain.event.AthleteActivityPublishedEvent;
import com.jovine360.talenthub.discovery.domain.model.AthleteSearchProjection;
import com.jovine360.talenthub.discovery.domain.model.SavedSearch;
import com.jovine360.talenthub.discovery.domain.repository.AthleteSearchProjectionRepository;
import com.jovine360.talenthub.discovery.domain.repository.SavedSearchRepository;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.service.EntitlementService;
import org.springframework.data.domain.Pageable;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AthleteDiscoveryService {

    private final AthleteSearchProjectionRepository projectionRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final EntitlementService entitlementService;

    public AthleteDiscoveryService(AthleteSearchProjectionRepository projectionRepository,
                                    SavedSearchRepository savedSearchRepository,
                                    EntitlementService entitlementService) {
        this.projectionRepository = projectionRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.entitlementService = entitlementService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AthleteSearchProjection> searchAthletes(String country, String ageCategory,
                                                                  Pageable pageable) {
        return PageResponse.from(
                projectionRepository.searchAthletes(country, ageCategory, pageable));
    }

    @Transactional(readOnly = true)
    public AthleteSearchProjection getDiscoveryProfile(String athleteId) {
        return projectionRepository.findByAthleteId(athleteId)
                .orElseThrow(() -> new EntityNotFoundException("AthleteSearchProjection", athleteId));
    }

    public SavedSearch saveSearch(String scoutProfileId, String name, String queryJson) {
        SavedSearch search = new SavedSearch();
        search.setScoutProfileId(scoutProfileId);
        search.setName(name);
        search.setQueryJson(queryJson);
        search.setCreatedAt(Instant.now());
        return savedSearchRepository.save(search);
    }

    @Transactional(readOnly = true)
    public List<SavedSearch> getSavedSearches(String scoutProfileId) {
        return savedSearchRepository.findByScoutProfileId(scoutProfileId);
    }

    // ---- Event listeners ------------------------------------------------

    @ApplicationModuleListener
    void onActivityPublished(AthleteActivityPublishedEvent event) {
        projectionRepository.findByAthleteId(event.athleteId()).ifPresent(p -> {
            p.setLastActivityDate(event.activityDate());
            p.setUpdatedAt(Instant.now());
            projectionRepository.save(p);
        });
    }
}
