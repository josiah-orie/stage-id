-- V3: Profile module tables.

CREATE TABLE IF NOT EXISTS user_profiles (
    id               CHAR(36)     NOT NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NULL,
    created_by       CHAR(36)     NULL,
    updated_by       CHAR(36)     NULL,
    user_id          CHAR(36)     NOT NULL,
    first_name       VARCHAR(100) NULL,
    last_name        VARCHAR(100) NULL,
    display_name     VARCHAR(150) NULL,
    date_of_birth    DATE         NULL,
    gender           VARCHAR(20)  NULL,
    phone_number     VARCHAR(30)  NULL,
    country_code     CHAR(2)      NULL,
    city             VARCHAR(100) NULL,
    bio              TEXT         NULL,
    avatar_url       VARCHAR(512) NULL,
    role             VARCHAR(50)  NOT NULL,
    profile_complete TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_profiles_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_profiles_user_id ON user_profiles (user_id);
CREATE INDEX idx_user_profiles_role    ON user_profiles (role);
