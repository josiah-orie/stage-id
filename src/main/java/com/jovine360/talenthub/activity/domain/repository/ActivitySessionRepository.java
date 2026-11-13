package com.jovine360.talenthub.activity.domain.repository;

import com.jovine360.talenthub.activity.domain.model.ActivitySession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivitySessionRepository extends JpaRepository<ActivitySession, String> {
    Page<ActivitySession> findByAthleteUserId(String athleteUserId, Pageable pageable);
    Page<ActivitySession> findByAthleteUserIdAndTemplateCode(String athleteUserId, String templateCode, Pageable pageable);
}
