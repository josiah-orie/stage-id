package com.jovine360.talenthub.scouting.domain.repository;

import com.jovine360.talenthub.scouting.domain.model.WatchlistAthlete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchlistAthleteRepository extends JpaRepository<WatchlistAthlete, String> {
    List<WatchlistAthlete> findByWatchlistId(String watchlistId);
    List<WatchlistAthlete> findByAthleteId(String athleteId);
    void deleteByWatchlistIdAndAthleteId(String watchlistId, String athleteId);
}
