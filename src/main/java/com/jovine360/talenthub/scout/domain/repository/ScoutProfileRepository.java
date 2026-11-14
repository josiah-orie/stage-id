package com.jovine360.talenthub.scout.domain.repository;

import com.jovine360.talenthub.scout.domain.model.ScoutProfile;
import com.jovine360.talenthub.scout.domain.model.ScoutVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoutProfileRepository extends JpaRepository<ScoutProfile, String> {
    Optional<ScoutProfile> findByUserAccountId(String userAccountId);
    boolean existsByUserAccountId(String userAccountId);
    org.springframework.data.domain.Page<ScoutProfile> findByVerificationStatus(
            com.jovine360.talenthub.scout.domain.model.ScoutVerificationStatus status,
            org.springframework.data.domain.Pageable pageable);
}
