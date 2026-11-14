package com.jovine360.talenthub.administration.application.service;

import com.jovine360.talenthub.administration.api.AuditLogDto;
import com.jovine360.talenthub.administration.domain.model.AuditLog;
import com.jovine360.talenthub.administration.domain.repository.AuditLogRepository;
import com.jovine360.talenthub.shared.domain.service.AuditTrail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class AuditLogService implements AuditTrail {

    /** Most privileged first; the first role the actor holds is stored as actor_role. */
    private static final List<String> ROLE_PRECEDENCE = List.of(
            "SUPER_ADMIN", "ADMIN", "ORG_ADMIN", "SCOUT", "COACH", "AGENT", "GUARDIAN", "PARENT", "ATHLETE");

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void record(String actorId, Collection<String> actorRoles, String action,
                       String entityType, String entityId, String details) {
        AuditLog log = new AuditLog();
        log.setCreatedAt(Instant.now());
        log.setActorId(actorId);
        log.setActorRole(primaryRole(actorRoles));
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        repository.save(log);
    }

    /**
     * ADMIN callers only ever see their own entries, whatever {@code actorId} they ask for.
     * SUPER_ADMIN callers see every actor and may filter by {@code actorId}.
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> search(String callerId, Collection<String> callerRoles,
                                    String actorId, String action, String entityType, String entityId,
                                    Instant from, Instant to, Pageable pageable) {
        boolean superAdmin = callerRoles != null && callerRoles.contains("SUPER_ADMIN");
        String effectiveActor = superAdmin ? blankToNull(actorId) : callerId;
        Pageable unsorted = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return repository.search(effectiveActor, blankToNull(action), blankToNull(entityType), blankToNull(entityId),
                from, to, unsorted).map(AuditLogService::toDto);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    private static String primaryRole(Collection<String> roles) {
        if (roles == null) return null;
        return ROLE_PRECEDENCE.stream().filter(roles::contains).findFirst()
                .orElse(roles.stream().findFirst().orElse(null));
    }

    private static AuditLogDto toDto(AuditLog a) {
        return new AuditLogDto(a.getId(), a.getCreatedAt(), a.getActorId(), a.getActorRole(),
                a.getAction(), a.getEntityType(), a.getEntityId(), a.getDetails());
    }
}
