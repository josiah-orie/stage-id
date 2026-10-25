package com.jovine360.talenthub.shared.domain.exception;

/**
 * Thrown when creating a resource that already exists (e.g., duplicate email).
 * Maps to HTTP 409 (Conflict) via the global exception handler.
 */
public class DuplicateResourceException extends RuntimeException {

    private final String resourceType;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String resourceType, String fieldName, Object fieldValue) {
        super(resourceType + " already exists with " + fieldName + ": " + fieldValue);
        this.resourceType = resourceType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public DuplicateResourceException(String message) {
        super(message);
        this.resourceType = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
