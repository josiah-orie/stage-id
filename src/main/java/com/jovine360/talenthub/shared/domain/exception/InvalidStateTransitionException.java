package com.jovine360.talenthub.shared.domain.exception;

/**
 * Thrown when an entity state transition is not permitted by the business rules.
 * Maps to HTTP 409 (Conflict) via the global exception handler.
 */
public class InvalidStateTransitionException extends RuntimeException {

    private final String entityType;
    private final String currentState;
    private final String targetState;

    public InvalidStateTransitionException(String entityType, String currentState, String targetState) {
        super("Cannot transition " + entityType + " from '" + currentState + "' to '" + targetState + "'");
        this.entityType = entityType;
        this.currentState = currentState;
        this.targetState = targetState;
    }

    public InvalidStateTransitionException(String message) {
        super(message);
        this.entityType = null;
        this.currentState = null;
        this.targetState = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getTargetState() {
        return targetState;
    }
}
