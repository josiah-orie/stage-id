package com.jovine360.talenthub.athlete.domain.repository;

import com.jovine360.talenthub.athlete.domain.model.AthleteSport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AthleteSportRepository extends JpaRepository<AthleteSport, String> {
    List<AthleteSport> findByAthleteUserId(String athleteUserId);
    Optional<AthleteSport> findByAthleteUserIdAndSportId(String athleteUserId, String sportId);
}
