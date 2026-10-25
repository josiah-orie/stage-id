package com.jovine360.talenthub.athleteaccess.domain.model;

/**
 * Permission levels for athlete profile access.
 */
public enum AthletePermission {
    /** View public profile information. */
    VIEW_PROFILE,
    /** View full stats and activity data. */
    VIEW_STATS,
    /** View private/medical documents. */
    VIEW_DOCUMENTS,
    /** Send messages to the athlete. */
    CONTACT,
    /** Full management access (guardian or org admin). */
    MANAGE
}
