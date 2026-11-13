package com.jovine360.talenthub.notification.application.service;

import com.jovine360.talenthub.notification.domain.model.*;
import com.jovine360.talenthub.notification.domain.repository.*;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailOutboxRepository emailOutboxRepository;

    public NotificationService(NotificationRepository notificationRepository,
                                NotificationPreferenceRepository preferenceRepository,
                                EmailOutboxRepository emailOutboxRepository) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.emailOutboxRepository = emailOutboxRepository;
    }

    public Notification createInApp(String recipientId, NotificationType type,
                                     String title, String bodyText) {
        Notification n = new Notification();
        n.setRecipientId(recipientId);
        n.setNotificationType(type);
        n.setTitle(title);
        n.setBodyText(bodyText);
        n.setCreatedAt(Instant.now());
        return notificationRepository.save(n);
    }

    public EmailOutbox queueEmail(String recipientEmail, String subject, String bodyHtml) {
        EmailOutbox email = new EmailOutbox();
        email.setRecipientEmail(recipientEmail);
        email.setSubject(subject);
        email.setBodyHtml(bodyHtml);
        email.setStatus("PENDING");
        email.setCreatedAt(Instant.now());
        return emailOutboxRepository.save(email);
    }

    public Notification markRead(String notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification", notificationId));
        n.setRead(true);
        n.setReadAt(Instant.now());
        return notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public PageResponse<Notification> getNotifications(String recipientId, Pageable pageable) {
        return PageResponse.from(
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable));
    }

    @Transactional(readOnly = true)
    public List<NotificationPreference> getPreferences(String userAccountId) {
        return preferenceRepository.findByUserAccountId(userAccountId);
    }

    public NotificationPreference upsertPreference(String userAccountId, NotificationType type,
                                                    boolean emailEnabled, boolean inAppEnabled) {
        NotificationPreference pref = preferenceRepository
                .findByUserAccountIdAndNotificationType(userAccountId, type)
                .orElseGet(() -> {
                    NotificationPreference p = new NotificationPreference();
                    p.setUserAccountId(userAccountId);
                    p.setNotificationType(type);
                    return p;
                });
        pref.setEmailEnabled(emailEnabled);
        pref.setInAppEnabled(inAppEnabled);
        return preferenceRepository.save(pref);
    }
}
