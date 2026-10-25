-- V9: Athlete access control tables.

CREATE TABLE IF NOT EXISTS athlete_access_grants (
    id              CHAR(36)    NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NULL,
    created_by      CHAR(36)    NULL,
    updated_by      CHAR(36)    NULL,
    athlete_user_id CHAR(36)    NOT NULL,
    grantee_id      CHAR(36)    NOT NULL,
    grantee_type    VARCHAR(30) NOT NULL,
    permission      VARCHAR(30) NOT NULL,
    expires_at      DATETIME(6) NULL,
    active          TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_access_grants_athlete  ON athlete_access_grants (athlete_user_id, active);
CREATE INDEX idx_access_grants_grantee  ON athlete_access_grants (grantee_id, grantee_type, active);
