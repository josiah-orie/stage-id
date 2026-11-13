-- V19: Notification module tables.

CREATE TABLE IF NOT EXISTS notifications (
    id                CHAR(36)    NOT NULL,
    recipient_id      CHAR(36)    NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    title             VARCHAR(200)NOT NULL,
    body_text         TEXT        NULL,
    `read`            TINYINT(1)  NOT NULL DEFAULT 0,
    read_at           DATETIME(6) NULL,
    created_at        DATETIME(6) NOT NULL,
    expires_at        DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_notif_recipient (recipient_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification_preferences (
    id                CHAR(36)    NOT NULL,
    user_account_id   CHAR(36)    NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    email_enabled     TINYINT(1)  NOT NULL DEFAULT 1,
    in_app_enabled    TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_np (user_account_id, notification_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS email_outbox (
    id              CHAR(36)    NOT NULL,
    recipient_email VARCHAR(254)NOT NULL,
    subject         VARCHAR(300)NOT NULL,
    body_html       TEXT        NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts        INT         NOT NULL DEFAULT 0,
    last_attempt_at DATETIME(6) NULL,
    error_message   VARCHAR(500)NULL,
    created_at      DATETIME(6) NOT NULL,
    sent_at         DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_eo_status (status, attempts)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification_delivery_attempts (
    id              CHAR(36)    NOT NULL,
    notification_id CHAR(36)    NOT NULL,
    channel         VARCHAR(20) NOT NULL,
    attempted_at    DATETIME(6) NOT NULL,
    success         TINYINT(1)  NOT NULL DEFAULT 0,
    error_message   VARCHAR(500)NULL,
    PRIMARY KEY (id),
    INDEX idx_nda_notif (notification_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
