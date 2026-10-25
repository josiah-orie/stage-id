package com.jovine360.talenthub.identity.domain.model;

/**
 * Lifecycle states of a user account.
 */
public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    SUSPENDED,
    DEACTIVATED
}
