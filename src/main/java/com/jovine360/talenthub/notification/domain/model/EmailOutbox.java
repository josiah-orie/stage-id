package com.jovine360.talenthub.notification.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "email_outbox")
@Getter
@Setter
public class EmailOutbox extends BaseEntity {

    @Column(name = "recipient_email", length = 254, nullable = false)
    private String recipientEmail;

    @Column(name = "subject", length = 300, nullable = false)
    private String subject;

    @Column(name = "body_html", columnDefinition = "TEXT")
    private String bodyHtml;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING"; // PENDING / SENT / FAILED

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;
}
