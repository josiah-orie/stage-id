package com.jovine360.talenthub.scouting.domain.repository;

import com.jovine360.talenthub.scouting.domain.model.ScoutNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoutNoteRepository extends JpaRepository<ScoutNote, String> {
    List<ScoutNote> findByScoutProfileIdOrderByCreatedAtDesc(String scoutProfileId);
    List<ScoutNote> findByScoutProfileIdAndAthleteIdOrderByCreatedAtDesc(String scoutProfileId, String athleteId);
}
