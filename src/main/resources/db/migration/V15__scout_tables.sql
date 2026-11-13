-- V15: Scout profile module tables.

CREATE TABLE IF NOT EXISTS scout_profiles (
    id                      CHAR(36)    NOT NULL,
    created_at              DATETIME(6) NOT NULL,
    updated_at              DATETIME(6) NULL,
    created_by              CHAR(36)    NULL,
    updated_by              CHAR(36)    NULL,
    user_account_id         CHAR(36)    NOT NULL,
    professional_title      VARCHAR(150)NULL,
    biography               TEXT        NULL,
    years_experience        INT         NULL,
    current_organization_id CHAR(36)    NULL,
    verification_status     VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    PRIMARY KEY (id),
    UNIQUE KEY uq_scout_user (user_account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS scout_specializations (
    id                   CHAR(36)    NOT NULL,
    created_at           DATETIME(6) NOT NULL,
    updated_at           DATETIME(6) NULL,
    created_by           CHAR(36)    NULL,
    updated_by           CHAR(36)    NULL,
    scout_profile_id     CHAR(36)    NOT NULL,
    sport_id             CHAR(36)    NOT NULL,
    level                VARCHAR(20) NULL,
    preferred_regions_json TEXT      NULL,
    PRIMARY KEY (id),
    INDEX idx_ss_profile (scout_profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS scout_verifications (
    id                           CHAR(36)    NOT NULL,
    scout_profile_id             CHAR(36)    NOT NULL,
    submitted_at                 DATETIME(6) NULL,
    reviewed_by                  CHAR(36)    NULL,
    reviewed_at                  DATETIME(6) NULL,
    status                       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    supporting_document_media_id CHAR(36)    NULL,
    notes                        TEXT        NULL,
    PRIMARY KEY (id),
    INDEX idx_sv_profile (scout_profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
