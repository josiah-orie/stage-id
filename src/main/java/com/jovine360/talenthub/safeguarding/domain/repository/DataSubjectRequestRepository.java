package com.jovine360.talenthub.safeguarding.domain.repository;

import com.jovine360.talenthub.safeguarding.domain.model.DataSubjectRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataSubjectRequestRepository extends JpaRepository<DataSubjectRequest, String> {
    List<DataSubjectRequest> findByRequesterIdOrderBySubmittedAtDesc(String requesterId);
    List<DataSubjectRequest> findByStatusOrderBySubmittedAtAsc(String status);
    List<DataSubjectRequest> findAllByOrderBySubmittedAtDesc();
}
