-- V8: Organization module tables with 9 org types.

CREATE TABLE IF NOT EXISTS organizations (
    id           CHAR(36)     NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NULL,
    created_by   CHAR(36)     NULL,
    updated_by   CHAR(36)     NULL,
    code         VARCHAR(10)  NOT NULL,
    name         VARCHAR(200) NOT NULL,
    org_type     VARCHAR(50)  NOT NULL,
    country_code CHAR(2)      NULL,
    city         VARCHAR(100) NULL,
    admin_user_id CHAR(36)    NOT NULL,
    logo_url     VARCHAR(512) NULL,
    status       VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    UNIQUE KEY uq_organizations_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_organizations_admin  ON organizations (admin_user_id);
CREATE INDEX idx_organizations_status ON organizations (status);

-- Org types are enforced via the OrgType enum:
-- CLUB, SCHOOL, ACADEMY, FEDERATION, NATIONAL_TEAM,
-- UNIVERSITY, COLLEGE, PROFESSIONAL_TEAM, SCOUTING_AGENCY
