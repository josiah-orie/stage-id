package com.jovine360.talenthub.notification.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification extends BaseEntity {

    @Column(name = "recipient_id", columnDefinition = "CHAR(36)", nullable = false)
    private String recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 40, nullable = false)
    private NotificationType notificationType;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    @Column(name = "read", nullable = false)
    private boolean read = false;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
