package com.jovine360.talenthub.scout.domain.repository;

import com.jovine360.talenthub.scout.domain.model.ScoutVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoutVerificationRepository extends JpaRepository<ScoutVerification, String> {
    List<ScoutVerification> findByScoutProfileId(String scoutProfileId);
}
