-- V14: Verification module tables.

CREATE TABLE IF NOT EXISTS activity_verifications (
    id          CHAR(36)    NOT NULL,
    activity_id CHAR(36)    NOT NULL,
    verifier_id CHAR(36)    NOT NULL,
    verified_at DATETIME(6) NOT NULL,
    notes       TEXT        NULL,
    PRIMARY KEY (id),
    INDEX idx_av_activity (activity_id),
    INDEX idx_av_verifier (verifier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
