package com.jovine360.talenthub.scouting.domain.repository;

import com.jovine360.talenthub.scouting.domain.model.ScoutFavourite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScoutFavouriteRepository extends JpaRepository<ScoutFavourite, String> {
    List<ScoutFavourite> findByScoutProfileId(String scoutProfileId);
    Optional<ScoutFavourite> findByScoutProfileIdAndAthleteId(String scoutProfileId, String athleteId);
    long countByScoutProfileId(String scoutProfileId);
}
