package com.jovine360.talenthub.verification.domain.repository;

import com.jovine360.talenthub.verification.domain.model.ActivityVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityVerificationRepository extends JpaRepository<ActivityVerification, String> {
    List<ActivityVerification> findByActivityId(String activityId);
}
