-- V26: Guardian relationships: typed links (PARENT, LEGAL_GUARDIAN, AGENT) with an optional
-- permission level, expiry, revocation and admin verification details.
ALTER TABLE guardian_links
    ADD COLUMN relationship_type         VARCHAR(30)  NOT NULL DEFAULT 'PARENT',
    ADD COLUMN permission                VARCHAR(30)  NULL,
    ADD COLUMN verification_document_ref VARCHAR(255) NULL,
    ADD COLUMN rejection_reason          VARCHAR(500) NULL,
    ADD COLUMN verified_at               DATETIME(6)  NULL,
    ADD COLUMN expires_at                DATETIME(6)  NULL,
    ADD COLUMN revoked_at                DATETIME(6)  NULL;

UPDATE guardian_links
   SET relationship_type = CASE
        WHEN UPPER(relationship) = 'AGENT'          THEN 'AGENT'
        WHEN UPPER(relationship) = 'LEGAL_GUARDIAN' THEN 'LEGAL_GUARDIAN'
        ELSE 'PARENT'
       END;

CREATE INDEX idx_guardian_links_status ON guardian_links (status, relationship_type);
