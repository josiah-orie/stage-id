-- V6: Athlete module tables.

CREATE TABLE IF NOT EXISTS athlete_number_sequences (
    id          CHAR(36)    NOT NULL,
    prefix_key  VARCHAR(30) NOT NULL,
    last_seq    BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_athlete_number_seq_prefix (prefix_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS athlete_profiles (
    id                  CHAR(36)    NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NULL,
    created_by          CHAR(36)    NULL,
    updated_by          CHAR(36)    NULL,
    user_id             CHAR(36)    NOT NULL,
    athlete_number      VARCHAR(30) NOT NULL,
    primary_sport_id    CHAR(36)    NULL,
    primary_sport_code  VARCHAR(10) NULL,
    position            VARCHAR(100)NULL,
    height_cm           INT         NULL,
    weight_kg           INT         NULL,
    dominant_foot       VARCHAR(10) NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    organization_id     CHAR(36)    NULL,
    nationality         CHAR(2)     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_athlete_profiles_user_id       (user_id),
    UNIQUE KEY uq_athlete_profiles_athlete_number (athlete_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_athlete_profiles_org     ON athlete_profiles (organization_id);
CREATE INDEX idx_athlete_profiles_sport   ON athlete_profiles (primary_sport_id);
CREATE INDEX idx_athlete_profiles_status  ON athlete_profiles (status);
