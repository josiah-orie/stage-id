-- V21: Safeguarding and consent tables.

CREATE TABLE IF NOT EXISTS consent_records (
    id                   CHAR(36)    NOT NULL,
    athlete_id           CHAR(36)    NOT NULL,
    purpose_code         VARCHAR(30) NOT NULL,
    consent_version      VARCHAR(20) NULL,
    consented_by         CHAR(36)    NOT NULL,
    consented_at         DATETIME(6) NOT NULL,
    revoked_at           DATETIME(6) NULL,
    revoked_by           CHAR(36)    NULL,
    authorization_source VARCHAR(20) NULL,
    PRIMARY KEY (id),
    INDEX idx_cr_athlete (athlete_id, purpose_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS athlete_privacy_preferences (
    id              CHAR(36)    NOT NULL,
    athlete_id      CHAR(36)    NOT NULL,
    field_group_code VARCHAR(60) NOT NULL,
    visibility_level VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    PRIMARY KEY (id),
    INDEX idx_app_athlete (athlete_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS safeguarding_reports (
    id                    CHAR(36)    NOT NULL,
    reporter_id           CHAR(36)    NOT NULL,
    reported_entity_id    CHAR(36)    NULL,
    reported_entity_type  VARCHAR(50) NULL,
    report_type           VARCHAR(40) NOT NULL,
    description           TEXT        NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitted_at          DATETIME(6) NOT NULL,
    resolved_at           DATETIME(6) NULL,
    resolved_by           CHAR(36)    NULL,
    PRIMARY KEY (id),
    INDEX idx_sr_reporter (reporter_id),
    INDEX idx_sr_status   (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS data_subject_requests (
    id           CHAR(36)    NOT NULL,
    requester_id CHAR(36)    NOT NULL,
    request_type VARCHAR(20) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitted_at DATETIME(6) NOT NULL,
    resolved_at  DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_dsr_requester (requester_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sensitive_access_audits (
    id           CHAR(36)    NOT NULL,
    accessor_id  CHAR(36)    NOT NULL,
    resource_type VARCHAR(60) NOT NULL,
    resource_id  CHAR(36)    NOT NULL,
    access_type  VARCHAR(60) NOT NULL,
    accessed_at  DATETIME(6) NOT NULL,
    justification VARCHAR(500) NULL,
    PRIMARY KEY (id),
    INDEX idx_saa_accessor  (accessor_id),
    INDEX idx_saa_resource  (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
