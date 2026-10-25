-- V5: Subscription stub tables with FREE_TIER plan seeded.

CREATE TABLE IF NOT EXISTS subscription_plans (
    id                CHAR(36)       NOT NULL,
    created_at        DATETIME(6)    NOT NULL,
    updated_at        DATETIME(6)    NULL,
    created_by        CHAR(36)       NULL,
    updated_by        CHAR(36)       NULL,
    code              VARCHAR(50)    NOT NULL,
    name              VARCHAR(100)   NOT NULL,
    target_role       VARCHAR(50)    NOT NULL,
    price_per_month   DECIMAL(10,2)  NULL,
    currency          CHAR(3)        NOT NULL DEFAULT 'USD',
    entitlements_json TEXT           NULL,
    active            TINYINT(1)     NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_subscription_plans_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_subscriptions (
    id            CHAR(36)    NOT NULL,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NULL,
    created_by    CHAR(36)    NULL,
    updated_by    CHAR(36)    NULL,
    subscriber_id CHAR(36)    NOT NULL,
    plan_id       CHAR(36)    NOT NULL,
    status        VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    starts_at     DATETIME(6) NOT NULL,
    expires_at    DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_user_subscriptions_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_subscriptions_subscriber ON user_subscriptions (subscriber_id, status);

-- Seed FREE_TIER plan
INSERT INTO subscription_plans (id, created_at, code, name, target_role, price_per_month, currency, entitlements_json, active)
VALUES (
    UUID(),
    NOW(6),
    'FREE_TIER',
    'Free Tier',
    'PLATFORM',
    0.00,
    'USD',
    '{"max_athletes":5,"max_scouts":0,"max_video_uploads":3,"max_org_members":10,"video_max_duration_s":120}',
    1
);
