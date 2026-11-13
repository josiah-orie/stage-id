package com.jovine360.talenthub.notification.application.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Strips sensitive keys from template context maps before rendering.
 */
@Component
public class NotificationPayloadSanitizer {

    private static final Set<String> BLOCKED_KEYS = Set.of(
            "password", "token", "secret", "dateOfBirth", "dob",
            "idNumber", "taxId", "medicalInfo", "ssn", "creditCard"
    );

    public Map<String, Object> sanitize(Map<String, Object> context) {
        if (context == null) return Map.of();
        Map<String, Object> safe = new HashMap<>();
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            boolean blocked = BLOCKED_KEYS.stream()
                    .anyMatch(k -> entry.getKey().toLowerCase().contains(k.toLowerCase()));
            if (!blocked) {
                safe.put(entry.getKey(), entry.getValue());
            }
        }
        return safe;
    }
}
