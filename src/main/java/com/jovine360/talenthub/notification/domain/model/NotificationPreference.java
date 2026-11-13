package com.jovine360.talenthub.notification.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
public class NotificationPreference extends BaseEntity {

    @Column(name = "user_account_id", columnDefinition = "CHAR(36)", nullable = false)
    private String userAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 40, nullable = false)
    private NotificationType notificationType;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled = true;
}
