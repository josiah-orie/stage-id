-- V27: Organization membership, invitations, athlete affiliations and verification details.
ALTER TABLE organizations
    ADD COLUMN description      VARCHAR(1000) NULL,
    ADD COLUMN verified_by      CHAR(36)      NULL,
    ADD COLUMN verified_at      DATETIME(6)   NULL,
    ADD COLUMN rejection_reason VARCHAR(500)  NULL;

CREATE TABLE IF NOT EXISTS organization_members (
    id              CHAR(36)    NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NULL,
    created_by      CHAR(36)    NULL,
    updated_by      CHAR(36)    NULL,
    organization_id CHAR(36)    NOT NULL,
    user_id         CHAR(36)    NOT NULL,
    member_role     VARCHAR(30) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_at       DATETIME(6) NOT NULL,
    removed_at      DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_org_members_org FOREIGN KEY (organization_id) REFERENCES organizations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_org_members_org  ON organization_members (organization_id, status);
CREATE INDEX idx_org_members_user ON organization_members (user_id, status);

CREATE TABLE IF NOT EXISTS organization_invitations (
    id              CHAR(36)     NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NULL,
    created_by      CHAR(36)     NULL,
    updated_by      CHAR(36)     NULL,
    organization_id CHAR(36)     NOT NULL,
    email           VARCHAR(255) NOT NULL,
    member_role     VARCHAR(30)  NOT NULL,
    token           VARCHAR(100) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    invited_by      CHAR(36)     NOT NULL,
    expires_at      DATETIME(6)  NOT NULL,
    accepted_by     CHAR(36)     NULL,
    accepted_at     DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_org_invitations_token (token),
    CONSTRAINT fk_org_invitations_org FOREIGN KEY (organization_id) REFERENCES organizations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_org_invitations_org ON organization_invitations (organization_id, status);

CREATE TABLE IF NOT EXISTS organization_athlete_relationships (
    id              CHAR(36)    NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NULL,
    created_by      CHAR(36)    NULL,
    updated_by      CHAR(36)    NULL,
    organization_id CHAR(36)    NOT NULL,
    athlete_user_id CHAR(36)    NOT NULL,
    permission      VARCHAR(30) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_by    CHAR(36)    NOT NULL,
    decided_at      DATETIME(6) NULL,
    ended_at        DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_org_athletes_org FOREIGN KEY (organization_id) REFERENCES organizations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_org_athletes_org     ON organization_athlete_relationships (organization_id, status);
CREATE INDEX idx_org_athletes_athlete ON organization_athlete_relationships (athlete_user_id, status);

-- Existing organizations: their admin becomes the OWNER member.
INSERT INTO organization_members (id, created_at, organization_id, user_id, member_role, status, joined_at)
SELECT UUID(), NOW(6), o.id, o.admin_user_id, 'OWNER', 'ACTIVE', NOW(6)
  FROM organizations o
 WHERE NOT EXISTS (SELECT 1 FROM organization_members m
                    WHERE m.organization_id = o.id AND m.user_id = o.admin_user_id);
