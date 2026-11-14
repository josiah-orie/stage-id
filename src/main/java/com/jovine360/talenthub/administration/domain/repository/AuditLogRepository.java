package com.jovine360.talenthub.administration.domain.repository;

import com.jovine360.talenthub.administration.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    @Query("SELECT a FROM AuditLog a WHERE (:actorId IS NULL OR a.actorId = :actorId) "
            + "AND (:action IS NULL OR a.action = :action) "
            + "AND (:entityType IS NULL OR a.entityType = :entityType) "
            + "AND (:entityId IS NULL OR a.entityId = :entityId) "
            + "AND (:from IS NULL OR a.createdAt >= :from) "
            + "AND (:to IS NULL OR a.createdAt < :to) "
            + "ORDER BY a.createdAt DESC")
    Page<AuditLog> search(@Param("actorId") String actorId, @Param("action") String action,
                          @Param("entityType") String entityType, @Param("entityId") String entityId,
                          @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);
}
