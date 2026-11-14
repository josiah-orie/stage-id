package com.jovine360.talenthub.scouting.application.service;

import com.jovine360.talenthub.scouting.domain.event.AthleteFollowedEvent;
import com.jovine360.talenthub.scouting.domain.model.*;
import com.jovine360.talenthub.scouting.domain.repository.*;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.service.EntitlementService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class WatchlistService {

    private final ScoutFavouriteRepository favouriteRepository;
    private final WatchlistRepository watchlistRepository;
    private final WatchlistAthleteRepository watchlistAthleteRepository;
    private final AthleteFollowRepository followRepository;
    private final EntitlementService entitlementService;
    private final ApplicationEventPublisher eventPublisher;

    public WatchlistService(ScoutFavouriteRepository favouriteRepository,
                             WatchlistRepository watchlistRepository,
                             WatchlistAthleteRepository watchlistAthleteRepository,
                             AthleteFollowRepository followRepository,
                             EntitlementService entitlementService,
                             ApplicationEventPublisher eventPublisher) {
        this.favouriteRepository = favouriteRepository;
        this.watchlistRepository = watchlistRepository;
        this.watchlistAthleteRepository = watchlistAthleteRepository;
        this.followRepository = followRepository;
        this.entitlementService = entitlementService;
        this.eventPublisher = eventPublisher;
    }

    public ScoutFavourite addFavourite(String scoutProfileId, String athleteId, String notes) {
        if (favouriteRepository.findByScoutProfileIdAndAthleteId(scoutProfileId, athleteId).isPresent()) {
            throw new com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException(
                    "Athlete is already in your favourites");
        }
        long current = favouriteRepository.countByScoutProfileId(scoutProfileId);
        entitlementService.assertWithinLimit(scoutProfileId, "MAX_FAVOURITES", current);
        ScoutFavourite fav = new ScoutFavourite();
        fav.setScoutProfileId(scoutProfileId);
        fav.setAthleteId(athleteId);
        fav.setAddedAt(Instant.now());
        fav.setNotes(notes);
        return favouriteRepository.save(fav);
    }

    public void removeFavourite(String scoutProfileId, String athleteId) {
        favouriteRepository.findByScoutProfileIdAndAthleteId(scoutProfileId, athleteId)
                .ifPresent(favouriteRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<ScoutFavourite> getFavourites(String scoutProfileId) {
        return favouriteRepository.findByScoutProfileId(scoutProfileId);
    }

    public Watchlist createWatchlist(String ownerId, OwnerType ownerType, String name,
                                      String description, boolean shared) {
        long current = watchlistRepository.countByOwnerId(ownerId);
        entitlementService.assertWithinLimit(ownerId, "MAX_WATCHLISTS", current);
        Watchlist watchlist = new Watchlist();
        watchlist.setOwnerId(ownerId);
        watchlist.setOwnerType(ownerType);
        watchlist.setName(name);
        watchlist.setDescription(description);
        watchlist.setShared(shared);
        return watchlistRepository.save(watchlist);
    }

    public WatchlistAthlete addToWatchlist(String watchlistId, String athleteId,
                                            String addedBy, String notes) {
        Watchlist watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new EntityNotFoundException("Watchlist", watchlistId));
        if (watchlistAthleteRepository.existsByWatchlistIdAndAthleteId(watchlistId, athleteId)) {
            throw new com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException(
                    "Athlete is already in this watchlist");
        }
        WatchlistAthlete wa = new WatchlistAthlete();
        wa.setWatchlistId(watchlistId);
        wa.setAthleteId(athleteId);
        wa.setAddedAt(Instant.now());
        wa.setAddedBy(addedBy);
        wa.setNotes(notes);
        WatchlistAthlete saved = watchlistAthleteRepository.save(wa);
        // Auto-follow
        followAthlete(watchlist.getOwnerId(), watchlist.getOwnerType(), athleteId);
        return saved;
    }

    public void removeFromWatchlist(String watchlistId, String athleteId) {
        watchlistAthleteRepository.deleteByWatchlistIdAndAthleteId(watchlistId, athleteId);
    }

    @Transactional(readOnly = true)
    public List<Watchlist> getWatchlistsOwnedBy(String ownerId) {
        return watchlistRepository.findByOwnerId(ownerId);
    }

    /** The owner can read any of their watchlists; others only shared ones. */
    @Transactional(readOnly = true)
    public Watchlist getReadableWatchlist(String watchlistId, String userId) {
        Watchlist w = getWatchlist(watchlistId);
        if (!w.getOwnerId().equals(userId) && !w.isShared()) {
            throw new SecurityException("This watchlist belongs to another user");
        }
        return w;
    }

    @Transactional(readOnly = true)
    public void assertOwner(String watchlistId, String userId) {
        if (!getWatchlist(watchlistId).getOwnerId().equals(userId)) {
            throw new SecurityException("Only the watchlist owner can change it");
        }
    }

    @Transactional(readOnly = true)
    public Watchlist getWatchlist(String watchlistId) {
        return watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new EntityNotFoundException("Watchlist", watchlistId));
    }

    /** Idempotent: following an athlete you already follow returns the existing follow. */
    public AthleteFollow followAthlete(String followerId, OwnerType followerType, String athleteId) {
        var existing = followRepository.findFirstByFollowerIdAndAthleteId(followerId, athleteId);
        if (existing.isPresent()) {
            return existing.get();
        }
        AthleteFollow follow = new AthleteFollow();
        follow.setFollowerId(followerId);
        follow.setFollowerType(followerType);
        follow.setAthleteId(athleteId);
        follow.setFollowedAt(Instant.now());
        follow.setNotificationsEnabled(true);
        AthleteFollow saved = followRepository.save(follow);
        eventPublisher.publishEvent(new AthleteFollowedEvent(followerId, followerType.name(), athleteId));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<AthleteFollow> getFollows(String followerId) {
        return followRepository.findByFollowerId(followerId);
    }

    public void unfollow(String followerId, String athleteId) {
        List<AthleteFollow> follows = followRepository.findByFollowerIdAndAthleteId(followerId, athleteId);
        if (follows.isEmpty()) {
            throw new EntityNotFoundException("AthleteFollow", athleteId);
        }
        followRepository.deleteAll(follows);
    }
}
