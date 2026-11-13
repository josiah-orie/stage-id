package com.jovine360.talenthub.notification.domain.repository;

import com.jovine360.talenthub.notification.domain.model.EmailOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, String> {
    List<EmailOutbox> findByStatusAndAttemptsLessThan(String status, int maxAttempts);
}
