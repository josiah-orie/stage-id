package com.jovine360.talenthub.shared.api;

/**
 * Represents a single field-level validation error in an {@link ApiError}.
 */
public record FieldError(
        String field,
        Object rejectedValue,
        String message
) {
    public static FieldError of(String field, Object rejectedValue, String message) {
        return new FieldError(field, rejectedValue, message);
    }
}
