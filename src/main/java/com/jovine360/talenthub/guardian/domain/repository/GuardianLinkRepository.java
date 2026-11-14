package com.jovine360.talenthub.guardian.domain.repository;

import com.jovine360.talenthub.guardian.domain.model.GuardianLink;
import com.jovine360.talenthub.guardian.domain.model.GuardianLinkStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuardianLinkRepository extends JpaRepository<GuardianLink, String> {
    List<GuardianLink> findByGuardianUserIdAndStatus(String guardianUserId, GuardianLinkStatus status);
    List<GuardianLink> findByAthleteUserIdAndStatus(String athleteUserId, GuardianLinkStatus status);
    List<GuardianLink> findByGuardianUserIdAndAthleteUserId(String guardianUserId, String athleteUserId);
    List<GuardianLink> findByGuardianUserIdOrAthleteUserIdOrderByCreatedAtDesc(String guardianUserId, String athleteUserId);
    Page<GuardianLink> findByStatus(GuardianLinkStatus status, Pageable pageable);
    boolean existsByGuardianUserIdAndAthleteUserIdAndStatus(String guardianUserId, String athleteUserId, GuardianLinkStatus status);
}
