-- V11: Full athlete activity tables.

CREATE TABLE IF NOT EXISTS athlete_activities (
    id                   CHAR(36)    NOT NULL,
    created_at           DATETIME(6) NOT NULL,
    updated_at           DATETIME(6) NULL,
    created_by           CHAR(36)    NULL,
    updated_by           CHAR(36)    NULL,
    athlete_id           CHAR(36)    NOT NULL,
    athlete_sport_profile_id CHAR(36) NULL,
    sport_id             CHAR(36)    NULL,
    activity_type_id     CHAR(36)    NOT NULL,
    schema_version_id    CHAR(36)    NOT NULL,
    title                VARCHAR(200)NOT NULL,
    description          TEXT        NULL,
    activity_date        DATE        NOT NULL,
    location             VARCHAR(200)NULL,
    organization_id      CHAR(36)    NULL,
    submitted_by         CHAR(36)    NULL,
    submitted_at         DATETIME(6) NULL,
    visibility_level     VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    activity_status      VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    verification_status  VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    PRIMARY KEY (id),
    INDEX idx_activities_athlete (athlete_id, activity_date),
    INDEX idx_activities_status  (activity_status),
    INDEX idx_activities_sport   (sport_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS athlete_activity_field_values (
    id                  CHAR(36)    NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NULL,
    created_by          CHAR(36)    NULL,
    updated_by          CHAR(36)    NULL,
    activity_id         CHAR(36)    NOT NULL,
    field_definition_id CHAR(36)    NOT NULL,
    field_code          VARCHAR(60) NOT NULL,
    string_value        TEXT        NULL,
    numeric_value       DECIMAL(18,4) NULL,
    boolean_value       TINYINT(1)  NULL,
    date_value          DATE        NULL,
    json_value          TEXT        NULL,
    PRIMARY KEY (id),
    INDEX idx_afv_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS athlete_activity_performances (
    id                   CHAR(36)    NOT NULL,
    created_at           DATETIME(6) NOT NULL,
    updated_at           DATETIME(6) NULL,
    created_by           CHAR(36)    NULL,
    updated_by           CHAR(36)    NULL,
    activity_id          CHAR(36)    NOT NULL,
    metric_definition_id CHAR(36)    NULL,
    metric_code          VARCHAR(60) NOT NULL,
    measured_value       DECIMAL(18,4) NULL,
    unit                 VARCHAR(30) NULL,
    measurement_context  VARCHAR(200)NULL,
    source               VARCHAR(20) NOT NULL DEFAULT 'SELF_REPORTED',
    PRIMARY KEY (id),
    INDEX idx_aap_activity (activity_id),
    INDEX idx_aap_metric   (metric_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS athlete_activity_media (
    id            CHAR(36)    NOT NULL,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NULL,
    created_by    CHAR(36)    NULL,
    updated_by    CHAR(36)    NULL,
    activity_id   CHAR(36)    NOT NULL,
    media_asset_id CHAR(36)   NOT NULL,
    media_type    VARCHAR(20) NOT NULL,
    caption       VARCHAR(300)NULL,
    ordinal       INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_aam_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS athlete_activity_revisions (
    id              CHAR(36)    NOT NULL,
    activity_id     CHAR(36)    NOT NULL,
    revision_number INT         NOT NULL,
    changed_by      CHAR(36)    NOT NULL,
    changed_at      DATETIME(6) NOT NULL,
    change_summary  VARCHAR(500)NULL,
    snapshot_json   TEXT        NULL,
    PRIMARY KEY (id),
    INDEX idx_aar_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS athlete_activity_participants (
    id               CHAR(36)    NOT NULL,
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NULL,
    created_by       CHAR(36)    NULL,
    updated_by       CHAR(36)    NULL,
    activity_id      CHAR(36)    NOT NULL,
    participant_type VARCHAR(20) NOT NULL,
    participant_id   CHAR(36)    NULL,
    participant_name VARCHAR(200)NULL,
    PRIMARY KEY (id),
    INDEX idx_aap2_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Keep legacy activity_sessions table for backward compatibility
CREATE TABLE IF NOT EXISTS activity_sessions (
    id              CHAR(36)    NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NULL,
    created_by      CHAR(36)    NULL,
    updated_by      CHAR(36)    NULL,
    athlete_user_id CHAR(36)    NOT NULL,
    template_code   VARCHAR(30) NOT NULL,
    session_date    DATE        NOT NULL,
    sport_code      VARCHAR(10) NULL,
    data_json       TEXT        NOT NULL,
    coach_notes     TEXT        NULL,
    logged_by       CHAR(36)    NULL,
    PRIMARY KEY (id),
    INDEX idx_activity_sessions_athlete   (athlete_user_id, session_date),
    INDEX idx_activity_sessions_template  (template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
