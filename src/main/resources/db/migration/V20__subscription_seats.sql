-- V20: Subscription seats for org scouting workspace.

CREATE TABLE IF NOT EXISTS subscription_seats (
    id                  CHAR(36)    NOT NULL,
    subscription_id     CHAR(36)    NOT NULL,
    seat_type           VARCHAR(20) NOT NULL DEFAULT 'SCOUT',
    assigned_to_user_id CHAR(36)    NULL,
    assigned_at         DATETIME(6) NULL,
    revoked_at          DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_seats_sub (subscription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
