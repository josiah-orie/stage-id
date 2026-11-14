package com.jovine360.talenthub.guardian.domain.model;

public enum GuardianLinkStatus {
    /** Awaiting the athlete's approval. */
    PENDING,
    /** Awaiting an administrator's review of the verification document. */
    PENDING_ADMIN_VERIFICATION,
    ACTIVE,
    REVOKED,
    REJECTED
}
