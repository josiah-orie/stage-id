package com.jovine360.talenthub.media.domain.model;

public enum ModerationStatus {
    /** Never reported, or approved by an admin. */
    CLEAN,
    /** Reported through a safeguarding report, awaiting admin review. Still visible. */
    FLAGGED,
    /** Hidden by an admin. Only the uploader, owner managers and admins can see it. */
    RESTRICTED
}
