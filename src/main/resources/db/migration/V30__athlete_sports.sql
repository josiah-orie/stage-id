-- V30: Secondary sports per athlete.
CREATE TABLE IF NOT EXISTS athlete_sports (
    id              CHAR(36)     NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NULL,
    created_by      CHAR(36)     NULL,
    updated_by      CHAR(36)     NULL,
    athlete_user_id CHAR(36)     NOT NULL,
    sport_id        CHAR(36)     NOT NULL,
    sport_code      VARCHAR(10)  NOT NULL,
    position        VARCHAR(100) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_athlete_sports (athlete_user_id, sport_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
