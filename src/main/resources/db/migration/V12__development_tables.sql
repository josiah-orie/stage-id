-- V12: Development module tables.

CREATE TABLE IF NOT EXISTS performance_measurements (
    id                      CHAR(36)    NOT NULL,
    created_at              DATETIME(6) NOT NULL,
    updated_at              DATETIME(6) NULL,
    created_by              CHAR(36)    NULL,
    updated_by              CHAR(36)    NULL,
    athlete_id              CHAR(36)    NOT NULL,
    athlete_sport_profile_id CHAR(36)  NULL,
    metric_definition_id    CHAR(36)    NULL,
    metric_code             VARCHAR(60) NOT NULL,
    measured_value          DECIMAL(18,4) NOT NULL,
    unit                    VARCHAR(30) NULL,
    observed_at             DATE        NOT NULL,
    measurement_context     VARCHAR(200)NULL,
    source_activity_id      CHAR(36)    NULL,
    source                  VARCHAR(20) NOT NULL DEFAULT 'SELF_REPORTED',
    verification_status     VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    verified_by             CHAR(36)    NULL,
    verified_at             DATETIME(6) NULL,
    recorded_by             CHAR(36)    NOT NULL,
    recorded_at             DATETIME(6) NOT NULL,
    corrected_from_id       CHAR(36)    NULL,
    PRIMARY KEY (id),
    INDEX idx_pm_athlete      (athlete_id, observed_at),
    INDEX idx_pm_metric       (athlete_id, metric_code),
    INDEX idx_pm_source_act   (source_activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS development_milestones (
    id                  CHAR(36)    NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NULL,
    created_by          CHAR(36)    NOT NULL,
    updated_by          CHAR(36)    NULL,
    athlete_id          CHAR(36)    NOT NULL,
    sport_id            CHAR(36)    NULL,
    milestone_type      VARCHAR(30) NOT NULL,
    title               VARCHAR(200)NOT NULL,
    description         TEXT        NULL,
    milestone_date      DATE        NOT NULL,
    evidence_activity_id CHAR(36)   NULL,
    PRIMARY KEY (id),
    INDEX idx_dm_athlete (athlete_id, milestone_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS performance_verifications (
    id             CHAR(36)    NOT NULL,
    measurement_id CHAR(36)    NOT NULL,
    verified_by    CHAR(36)    NOT NULL,
    verified_at    DATETIME(6) NOT NULL,
    method         VARCHAR(100)NULL,
    notes          TEXT        NULL,
    PRIMARY KEY (id),
    INDEX idx_pv_measurement (measurement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
