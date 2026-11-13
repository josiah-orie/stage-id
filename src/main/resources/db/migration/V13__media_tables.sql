-- V13: Media module tables.

CREATE TABLE IF NOT EXISTS media_assets (
    id                CHAR(36)    NOT NULL,
    created_at        DATETIME(6) NOT NULL,
    updated_at        DATETIME(6) NULL,
    created_by        CHAR(36)    NULL,
    updated_by        CHAR(36)    NULL,
    owner_id          CHAR(36)    NOT NULL,
    owner_type        VARCHAR(20) NOT NULL,
    asset_type        VARCHAR(30) NOT NULL,
    original_filename VARCHAR(500)NULL,
    mime_type         VARCHAR(100)NULL,
    size_bytes        BIGINT      NULL,
    bucket_name       VARCHAR(100)NULL,
    object_key        VARCHAR(1000)NULL,
    processing_status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    uploaded_at       DATETIME(6) NULL,
    processed_at      DATETIME(6) NULL,
    deleted_at        DATETIME(6) NULL,
    content_hash      VARCHAR(64) NULL,
    uploaded_by       CHAR(36)    NULL,
    PRIMARY KEY (id),
    INDEX idx_media_owner    (owner_id, processing_status),
    INDEX idx_media_type     (asset_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS media_upload_sessions (
    id               CHAR(36)    NOT NULL,
    media_asset_id   CHAR(36)    NOT NULL,
    presigned_url    TEXT        NULL,
    presigned_expiry DATETIME(6) NULL,
    session_status   VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at       DATETIME(6) NOT NULL,
    completed_at     DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_mus_asset (media_asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS media_processing_jobs (
    id              CHAR(36)    NOT NULL,
    media_asset_id  CHAR(36)    NOT NULL,
    job_type        VARCHAR(30) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    attempts        INT         NOT NULL DEFAULT 0,
    last_attempt_at DATETIME(6) NULL,
    error_message   VARCHAR(500)NULL,
    created_at      DATETIME(6) NOT NULL,
    completed_at    DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_mpj_asset  (media_asset_id),
    INDEX idx_mpj_status (status, attempts)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS media_variants (
    id             CHAR(36)     NOT NULL,
    media_asset_id CHAR(36)     NOT NULL,
    variant_type   VARCHAR(20)  NOT NULL,
    object_key     VARCHAR(1000)NULL,
    width          INT          NULL,
    height         INT          NULL,
    size_bytes     BIGINT       NULL,
    created_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_mv_asset (media_asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS media_access_audits (
    id             CHAR(36)    NOT NULL,
    media_asset_id CHAR(36)    NOT NULL,
    accessed_by    CHAR(36)    NOT NULL,
    accessed_at    DATETIME(6) NOT NULL,
    access_type    VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_maa_asset (media_asset_id),
    INDEX idx_maa_user  (accessed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
