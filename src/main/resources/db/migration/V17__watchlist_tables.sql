-- V17: Scout favourites, watchlists, follows tables.

CREATE TABLE IF NOT EXISTS scout_favourites (
    id               CHAR(36)    NOT NULL,
    scout_profile_id CHAR(36)    NOT NULL,
    athlete_id       CHAR(36)    NOT NULL,
    added_at         DATETIME(6) NOT NULL,
    notes            TEXT        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_sf (scout_profile_id, athlete_id),
    INDEX idx_sf_scout (scout_profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS watchlists (
    id          CHAR(36)    NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    owner_id    CHAR(36)    NOT NULL,
    owner_type  VARCHAR(20) NOT NULL,
    name        VARCHAR(150)NOT NULL,
    description TEXT        NULL,
    is_shared   TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_wl_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS watchlist_athletes (
    id           CHAR(36)    NOT NULL,
    watchlist_id CHAR(36)    NOT NULL,
    athlete_id   CHAR(36)    NOT NULL,
    added_at     DATETIME(6) NOT NULL,
    added_by     CHAR(36)    NULL,
    notes        TEXT        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_wa (watchlist_id, athlete_id),
    INDEX idx_wa_watchlist (watchlist_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS athlete_follows (
    id                    CHAR(36)    NOT NULL,
    follower_id           CHAR(36)    NOT NULL,
    follower_type         VARCHAR(20) NOT NULL,
    athlete_id            CHAR(36)    NOT NULL,
    followed_at           DATETIME(6) NOT NULL,
    notifications_enabled TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    INDEX idx_af_athlete (athlete_id),
    INDEX idx_af_follower (follower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
