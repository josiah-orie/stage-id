package com.jovine360.talenthub.athlete.domain.repository;

import com.jovine360.talenthub.athlete.domain.model.AthleteProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AthleteProfileRepository extends JpaRepository<AthleteProfile, String> {
    Optional<AthleteProfile> findByUserId(String userId);
    boolean existsByUserId(String userId);
    Optional<AthleteProfile> findByAthleteNumber(String athleteNumber);
}
