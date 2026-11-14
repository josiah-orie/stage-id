package com.jovine360.talenthub.guardian.domain.model;

/** Kind of relationship between a user and an athlete. */
public enum GuardianRelationshipType {
    /** Parent. Needs the athlete's approval. Full management access. */
    PARENT,
    /** Legal guardian. Needs a verification document and admin approval. Expires after a year. */
    LEGAL_GUARDIAN,
    /** Sports agent. Needs the athlete's approval. Access is a scoped access grant. */
    AGENT
}
