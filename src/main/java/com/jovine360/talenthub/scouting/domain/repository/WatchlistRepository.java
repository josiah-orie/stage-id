package com.jovine360.talenthub.scouting.domain.repository;

import com.jovine360.talenthub.scouting.domain.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchlistRepository extends JpaRepository<Watchlist, String> {
    List<Watchlist> findByOwnerId(String ownerId);
    long countByOwnerId(String ownerId);
}
