package com.jovine360.talenthub.scouting.application.service;

import com.jovine360.talenthub.scouting.domain.model.*;
import com.jovine360.talenthub.scouting.domain.repository.*;
import com.jovine360.talenthub.shared.domain.exception.EntitlementLimitExceededException;
import com.jovine360.talenthub.shared.domain.service.EntitlementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class OrganizationScoutingWorkspaceService {

    private final SubscriptionSeatRepository seatRepository;
    private final WatchlistRepository watchlistRepository;
    private final ScoutEvaluationRepository evaluationRepository;
    private final EntitlementService entitlementService;

    public OrganizationScoutingWorkspaceService(SubscriptionSeatRepository seatRepository,
                                                 WatchlistRepository watchlistRepository,
                                                 ScoutEvaluationRepository evaluationRepository,
                                                 EntitlementService entitlementService) {
        this.seatRepository = seatRepository;
        this.watchlistRepository = watchlistRepository;
        this.evaluationRepository = evaluationRepository;
        this.entitlementService = entitlementService;
    }

    public void activateWorkspace(String orgId, String actorId) {
        if (!entitlementService.hasActivePlan(orgId)) {
            throw new EntitlementLimitExceededException("SHARED_SCOUTING_WORKSPACE feature not available");
        }
    }

    public SubscriptionSeat addScoutSeat(String subscriptionId, String orgId, String userId) {
        long current = seatRepository.countBySubscriptionIdAndRevokedAtIsNull(subscriptionId);
        entitlementService.assertWithinLimit(orgId, "MAX_SCOUT_SEATS", current);
        SubscriptionSeat seat = new SubscriptionSeat();
        seat.setSubscriptionId(subscriptionId);
        seat.setAssignedToUserId(userId);
        seat.setAssignedAt(Instant.now());
        return seatRepository.save(seat);
    }

    public void revokeScoutSeat(String seatId) {
        seatRepository.findById(seatId).ifPresent(seat -> {
            seat.setRevokedAt(Instant.now());
            seatRepository.save(seat);
        });
    }

    @Transactional(readOnly = true)
    public List<Watchlist> getSharedWatchlists(String orgId) {
        return watchlistRepository.findByOwnerId(orgId);
    }

    @Transactional(readOnly = true)
    public List<ScoutEvaluation> getOrgEvaluations(String orgId) {
        return evaluationRepository.findByAthleteId(orgId); // scoped to org's athletes
    }

    public Watchlist createOrgWatchlist(String orgId, String name, String description) {
        Watchlist watchlist = new Watchlist();
        watchlist.setOwnerId(orgId);
        watchlist.setOwnerType(OwnerType.ORGANIZATION);
        watchlist.setName(name);
        watchlist.setDescription(description);
        watchlist.setShared(true);
        return watchlistRepository.save(watchlist);
    }
}
