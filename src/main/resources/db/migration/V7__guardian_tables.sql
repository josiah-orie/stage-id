-- V7: Guardian module tables.

CREATE TABLE IF NOT EXISTS guardian_links (
    id                  CHAR(36)    NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NULL,
    created_by          CHAR(36)    NULL,
    updated_by          CHAR(36)    NULL,
    guardian_user_id    CHAR(36)    NOT NULL,
    athlete_user_id     CHAR(36)    NOT NULL,
    relationship        VARCHAR(50) NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    consent_given       TINYINT(1)  NOT NULL DEFAULT 0,
    consent_given_at    DATETIME(6) NULL,
    verified_by         CHAR(36)    NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_guardian_links_guardian ON guardian_links (guardian_user_id, status);
CREATE INDEX idx_guardian_links_athlete  ON guardian_links (athlete_user_id, status);
