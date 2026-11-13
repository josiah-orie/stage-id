-- V16: Athlete discovery module tables.

CREATE TABLE IF NOT EXISTS athlete_search_projections (
    id                   CHAR(36)    NOT NULL,
    athlete_id           CHAR(36)    NOT NULL,
    athlete_number       VARCHAR(30) NULL,
    display_name         VARCHAR(200)NULL,
    sport_ids_json       TEXT        NULL,
    discipline_ids_json  TEXT        NULL,
    position_ids_json    TEXT        NULL,
    country_of_residence CHAR(2)     NULL,
    state_region         VARCHAR(100)NULL,
    age_category         VARCHAR(20) NULL,
    verification_status  VARCHAR(20) NULL,
    profile_status       VARCHAR(20) NULL,
    last_activity_date   DATE        NULL,
    top_metrics_json     TEXT        NULL,
    visibility_level     VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    updated_at           DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_asp_athlete (athlete_id),
    INDEX idx_asp_country   (country_of_residence, visibility_level),
    INDEX idx_asp_age       (age_category, visibility_level),
    INDEX idx_asp_sport     (sport_ids_json(50)),
    INDEX idx_asp_activity  (last_activity_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS saved_searches (
    id               CHAR(36)    NOT NULL,
    scout_profile_id CHAR(36)    NOT NULL,
    name             VARCHAR(150)NOT NULL,
    query_json       TEXT        NULL,
    created_at       DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_ss_scout (scout_profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
