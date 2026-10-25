package com.jovine360.talenthub.sport.api;

/**
 * Read model for a sport.
 */
public record SportDto(
        String id,
        String code,
        String name,
        String description,
        boolean active
) {
}
