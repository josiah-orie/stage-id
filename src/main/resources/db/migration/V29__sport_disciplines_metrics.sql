-- V29: Sport disciplines (e.g. 100m within Athletics) and performance metric definitions.
CREATE TABLE IF NOT EXISTS sport_disciplines (
    id          CHAR(36)     NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NULL,
    created_by  CHAR(36)     NULL,
    updated_by  CHAR(36)     NULL,
    sport_id    CHAR(36)     NOT NULL,
    code        VARCHAR(30)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    active      TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_sport_disciplines (sport_id, code),
    CONSTRAINT fk_sport_disciplines_sport FOREIGN KEY (sport_id) REFERENCES sports (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sport_metric_definitions (
    id               CHAR(36)     NOT NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NULL,
    created_by       CHAR(36)     NULL,
    updated_by       CHAR(36)     NULL,
    sport_id         CHAR(36)     NOT NULL,
    code             VARCHAR(50)  NOT NULL,
    name             VARCHAR(100) NOT NULL,
    unit             VARCHAR(20)  NULL,
    higher_is_better TINYINT(1)   NOT NULL DEFAULT 1,
    active           TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_sport_metrics (sport_id, code),
    CONSTRAINT fk_sport_metrics_sport FOREIGN KEY (sport_id) REFERENCES sports (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
