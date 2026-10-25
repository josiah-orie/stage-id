package com.jovine360.talenthub.shared.domain.exception;

/**
 * Thrown when a requested entity does not exist.
 * Maps to HTTP 404 via the global exception handler.
 */
public class EntityNotFoundException extends RuntimeException {

    private final String entityType;
    private final String entityId;

    public EntityNotFoundException(String entityType, String entityId) {
        super(entityType + " not found with id: " + entityId);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public EntityNotFoundException(Class<?> entityClass, String entityId) {
        this(entityClass.getSimpleName(), entityId);
    }

    public EntityNotFoundException(String message) {
        super(message);
        this.entityType = "Unknown";
        this.entityId = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }
}
