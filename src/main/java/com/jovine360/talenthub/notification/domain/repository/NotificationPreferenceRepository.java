package com.jovine360.talenthub.notification.domain.repository;

import com.jovine360.talenthub.notification.domain.model.NotificationPreference;
import com.jovine360.talenthub.notification.domain.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, String> {
    Optional<NotificationPreference> findByUserAccountIdAndNotificationType(
            String userAccountId, NotificationType type);
    List<NotificationPreference> findByUserAccountId(String userAccountId);
}
