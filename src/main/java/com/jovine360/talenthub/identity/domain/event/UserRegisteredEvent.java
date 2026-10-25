package com.jovine360.talenthub.identity.domain.event;

import com.jovine360.talenthub.identity.domain.model.UserRole;
import org.springframework.modulith.events.Externalized;

/**
 * Published when a new user account is successfully created.
 * Consumed by the profile module to create an initial profile record.
 */
public record UserRegisteredEvent(
        String userId,
        String email,
        UserRole role
) {
}
