package com.jovine360.talenthub.shared.domain.service;

import java.util.Collection;

/**
 * Records administrative actions in the audit log. Implemented by the administration module;
 * any module may call it.
 */
public interface AuditTrail {

    /**
     * @param actorId    user performing the action
     * @param actorRoles the actor's roles; the most privileged one is stored
     * @param action     e.g. USER_SUSPENDED, ORGANIZATION_VERIFIED
     * @param entityType e.g. User, Organization
     * @param entityId   id of the affected record
     * @param details    optional free text or JSON, stored as the after-state
     */
    void record(String actorId, Collection<String> actorRoles, String action,
                String entityType, String entityId, String details);
}
