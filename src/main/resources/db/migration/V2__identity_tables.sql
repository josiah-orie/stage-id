-- V2: Identity module tables — user accounts and refresh tokens.

CREATE TABLE IF NOT EXISTS users (
    id                                  CHAR(36)     NOT NULL,
    created_at                          DATETIME(6)  NOT NULL,
    updated_at                          DATETIME(6)  NULL,
    created_by                          CHAR(36)     NULL,
    updated_by                          CHAR(36)     NULL,
    email                               VARCHAR(255) NOT NULL,
    password_hash                       VARCHAR(72)  NOT NULL,
    role                                VARCHAR(50)  NOT NULL,
    status                              VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified                      TINYINT(1)   NOT NULL DEFAULT 0,
    email_verification_token            VARCHAR(100) NULL,
    email_verification_token_expires_at DATETIME(6)  NULL,
    password_reset_token                VARCHAR(100) NULL,
    password_reset_token_expires_at     DATETIME(6)  NULL,
    last_login_at                       DATETIME(6)  NULL,
    failed_login_attempts               INT          NOT NULL DEFAULT 0,
    locked_until                        DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_email              ON users (email);
CREATE INDEX idx_users_status             ON users (status);
CREATE INDEX idx_users_email_verify_token ON users (email_verification_token);
CREATE INDEX idx_users_password_reset_tok ON users (password_reset_token);

-- -----------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id           CHAR(36)     NOT NULL,
    user_id      CHAR(36)     NOT NULL,
    token_hash   VARCHAR(72)  NOT NULL,
    expires_at   DATETIME(6)  NOT NULL,
    revoked      TINYINT(1)   NOT NULL DEFAULT 0,
    device_info  VARCHAR(255) NULL,
    ip_address   VARCHAR(45)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_refresh_tokens_user_id  ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_revoked  ON refresh_tokens (revoked, expires_at);
