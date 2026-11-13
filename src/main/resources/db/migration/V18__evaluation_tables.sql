-- V18: Scout evaluation tables.

CREATE TABLE IF NOT EXISTS scout_evaluations (
    id                       CHAR(36)      NOT NULL,
    created_at               DATETIME(6)   NOT NULL,
    updated_at               DATETIME(6)   NULL,
    created_by               CHAR(36)      NULL,
    updated_by               CHAR(36)      NULL,
    scout_profile_id         CHAR(36)      NOT NULL,
    athlete_id               CHAR(36)      NOT NULL,
    athlete_sport_profile_id CHAR(36)      NULL,
    sport_id                 CHAR(36)      NULL,
    evaluation_title         VARCHAR(200)  NOT NULL,
    overall_rating           DECIMAL(3,1)  NULL,
    status                   VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    evaluation_date          DATE          NULL,
    PRIMARY KEY (id),
    INDEX idx_se_scout   (scout_profile_id),
    INDEX idx_se_athlete (athlete_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS evaluation_criteria (
    id             CHAR(36)    NOT NULL,
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NULL,
    created_by     CHAR(36)    NULL,
    updated_by     CHAR(36)    NULL,
    sport_id       CHAR(36)    NULL,
    criterion_code VARCHAR(60) NOT NULL,
    criterion_name VARCHAR(150)NOT NULL,
    description    TEXT        NULL,
    category       VARCHAR(100)NULL,
    display_order  INT         NOT NULL DEFAULT 0,
    active         TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    INDEX idx_ec_sport (sport_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS evaluation_scores (
    id            CHAR(36)     NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NULL,
    created_by    CHAR(36)     NULL,
    updated_by    CHAR(36)     NULL,
    evaluation_id CHAR(36)     NOT NULL,
    criterion_id  CHAR(36)     NOT NULL,
    score         DECIMAL(3,1) NULL,
    max_score     DECIMAL(3,1) NOT NULL DEFAULT 10.0,
    notes         TEXT         NULL,
    PRIMARY KEY (id),
    INDEX idx_es_eval (evaluation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS scout_notes (
    id               CHAR(36)    NOT NULL,
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NULL,
    created_by       CHAR(36)    NULL,
    updated_by       CHAR(36)    NULL,
    scout_profile_id CHAR(36)    NOT NULL,
    athlete_id       CHAR(36)    NOT NULL,
    note_text        TEXT        NOT NULL,
    note_date        DATE        NULL,
    is_private       TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    INDEX idx_sn_scout   (scout_profile_id),
    INDEX idx_sn_athlete (athlete_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- Seed evaluation criteria ------------------------------------------
-- Football criteria
INSERT INTO evaluation_criteria (id, created_at, sport_id, criterion_code, criterion_name, category, display_order, active) VALUES
(UUID(), NOW(6), NULL, 'FB_TECHNICAL',  'Technical',  'Football', 1, 1),
(UUID(), NOW(6), NULL, 'FB_TACTICAL',   'Tactical',   'Football', 2, 1),
(UUID(), NOW(6), NULL, 'FB_PHYSICAL',   'Physical',   'Football', 3, 1),
(UUID(), NOW(6), NULL, 'FB_MENTAL',     'Mental',     'Football', 4, 1),
(UUID(), NOW(6), NULL, 'FB_POTENTIAL',  'Potential',  'Football', 5, 1);

-- Basketball criteria
INSERT INTO evaluation_criteria (id, created_at, sport_id, criterion_code, criterion_name, category, display_order, active) VALUES
(UUID(), NOW(6), NULL, 'BB_OFFENSE',     'Offense',     'Basketball', 1, 1),
(UUID(), NOW(6), NULL, 'BB_DEFENSE',     'Defense',     'Basketball', 2, 1),
(UUID(), NOW(6), NULL, 'BB_ATHLETICISM', 'Athleticism', 'Basketball', 3, 1),
(UUID(), NOW(6), NULL, 'BB_IQ',          'Basketball IQ','Basketball',4, 1),
(UUID(), NOW(6), NULL, 'BB_POTENTIAL',   'Potential',   'Basketball', 5, 1);
