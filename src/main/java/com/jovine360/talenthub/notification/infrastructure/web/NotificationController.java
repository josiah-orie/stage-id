package com.jovine360.talenthub.notification.infrastructure.web;

import com.jovine360.talenthub.notification.application.service.NotificationService;
import com.jovine360.talenthub.notification.domain.model.Notification;
import com.jovine360.talenthub.notification.domain.model.NotificationPreference;
import com.jovine360.talenthub.notification.domain.model.NotificationType;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<Notification>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotifications(principal.userId(), pageable));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markRead(@PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        // Someone else's notification is reported as not found, so ids cannot be probed
        return ResponseEntity.ok(notificationService.markRead(id, principal.userId()));
    }

    @GetMapping("/preferences")
    public ResponseEntity<List<NotificationPreference>> getPreferences(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(notificationService.getPreferences(principal.userId()));
    }

    record PreferenceRequest(NotificationType notificationType,
                              boolean emailEnabled, boolean inAppEnabled) {}

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreference> updatePreference(
            @RequestBody PreferenceRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(notificationService.upsertPreference(
                principal.userId(), req.notificationType(), req.emailEnabled(), req.inAppEnabled()));
    }
}
