package com.jovine360.talenthub.administration.api;

import java.time.Instant;

public record AuditLogDto(
        String id, Instant createdAt, String actorId, String actorRole,
        String action, String entityType, String entityId, String details
) {}
