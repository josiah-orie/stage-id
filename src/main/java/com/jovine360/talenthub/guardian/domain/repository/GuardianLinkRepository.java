package com.jovine360.talenthub.guardian.domain.repository;

import com.jovine360.talenthub.guardian.domain.model.GuardianLink;
import com.jovine360.talenthub.guardian.domain.model.GuardianLinkStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuardianLinkRepository extends JpaRepository<GuardianLink, String> {
    List<GuardianLink> findByGuardianUserIdAndStatus(String guardianUserId, GuardianLinkStatus status);
    List<GuardianLink> findByAthleteUserIdAndStatus(String athleteUserId, GuardianLinkStatus status);
    Optional<GuardianLink> findByGuardianUserIdAndAthleteUserId(String guardianUserId, String athleteUserId);
    boolean existsByGuardianUserIdAndAthleteUserIdAndStatus(String guardianUserId, String athleteUserId, GuardianLinkStatus status);
}
