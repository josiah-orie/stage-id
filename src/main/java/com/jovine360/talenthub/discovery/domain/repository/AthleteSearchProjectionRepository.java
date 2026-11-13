package com.jovine360.talenthub.discovery.domain.repository;

import com.jovine360.talenthub.discovery.domain.model.AthleteSearchProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AthleteSearchProjectionRepository extends JpaRepository<AthleteSearchProjection, String> {
    Optional<AthleteSearchProjection> findByAthleteId(String athleteId);

    @Query("SELECT p FROM AthleteSearchProjection p WHERE p.visibilityLevel != 'PRIVATE' " +
           "AND (:country IS NULL OR p.countryOfResidence = :country) " +
           "AND (:ageCategory IS NULL OR p.ageCategory = :ageCategory)")
    Page<AthleteSearchProjection> searchAthletes(String country, String ageCategory, Pageable pageable);
}
