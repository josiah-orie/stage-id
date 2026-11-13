-- V22: Full subscription lifecycle — extend V5 tables.

ALTER TABLE subscription_plans
    ADD COLUMN IF NOT EXISTS trial_days    INT          NULL,
    ADD COLUMN IF NOT EXISTS is_public     TINYINT(1)   NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS sort_order    INT          NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS subscription_plan_prices (
    id             CHAR(36)      NOT NULL,
    created_at     DATETIME(6)   NOT NULL,
    updated_at     DATETIME(6)   NULL,
    created_by     CHAR(36)      NULL,
    updated_by     CHAR(36)      NULL,
    plan_id        CHAR(36)      NOT NULL,
    currency       CHAR(3)       NOT NULL,
    amount         DECIMAL(10,2) NOT NULL,
    billing_period VARCHAR(20)   NOT NULL,
    active         TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    INDEX idx_spp_plan (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS plan_features (
    id              CHAR(36)    NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NULL,
    created_by      CHAR(36)    NULL,
    updated_by      CHAR(36)    NULL,
    plan_id         CHAR(36)    NOT NULL,
    entitlement_key VARCHAR(80) NOT NULL,
    limit_value     BIGINT      NULL,
    unlimited       TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_pf_plan (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE user_subscriptions
    ADD COLUMN IF NOT EXISTS grace_ends_at       DATETIME(6) NULL,
    ADD COLUMN IF NOT EXISTS cancelled_at        DATETIME(6) NULL,
    ADD COLUMN IF NOT EXISTS cancelled_by        CHAR(36)    NULL,
    ADD COLUMN IF NOT EXISTS cancellation_reason VARCHAR(500)NULL,
    ADD COLUMN IF NOT EXISTS renews_at           DATETIME(6) NULL;

CREATE TABLE IF NOT EXISTS subscription_addons (
    id              CHAR(36)    NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NULL,
    created_by      CHAR(36)    NULL,
    updated_by      CHAR(36)    NULL,
    subscription_id CHAR(36)    NOT NULL,
    addon_code      VARCHAR(50) NOT NULL,
    quantity        INT         NOT NULL DEFAULT 1,
    active          TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    INDEX idx_sa_sub (subscription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- Seed 11 plan types -------------------------------------------------
SET @plan_athlete_free  = UUID();
SET @plan_guardian      = UUID();
SET @plan_scout_ind     = UUID();
SET @plan_scout_pro     = UUID();
SET @plan_coach_agent   = UUID();
SET @plan_school        = UUID();
SET @plan_academy       = UUID();
SET @plan_club          = UUID();
SET @plan_scouting_agency = UUID();
SET @plan_federation    = UUID();
SET @plan_enterprise    = UUID();

INSERT INTO subscription_plans (id, created_at, code, name, target_role, price_per_month, currency,
    entitlements_json, active, trial_days, is_public, sort_order) VALUES
(@plan_athlete_free, NOW(6), 'ATHLETE_FREE',       'Athlete Free',            'ATHLETE', 0.00, 'USD', '{"max_video_uploads":3,"max_org_members":0}', 1, 0, 1, 1),
(@plan_guardian,     NOW(6), 'PARENT_GUARDIAN',    'Parent / Guardian',       'GUARDIAN',4.99, 'USD', '{"max_athletes":5}', 1, 14, 1, 2),
(@plan_scout_ind,    NOW(6), 'SCOUT_INDIVIDUAL',   'Individual Scout',        'SCOUT',   9.99, 'USD', '{"MAX_FAVOURITES":50,"MAX_WATCHLISTS":5}', 1, 14, 1, 3),
(@plan_scout_pro,    NOW(6), 'SCOUT_PRO',          'Scout Pro',               'SCOUT',  19.99, 'USD', '{"MAX_FAVOURITES":200,"MAX_WATCHLISTS":20,"SCOUT_EVALUATIONS":1,"ADVANCED_ATHLETE_SEARCH":1}', 1, 14, 1, 4),
(@plan_coach_agent,  NOW(6), 'COACH_AGENT',        'Coach / Agent',           'COACH',  14.99, 'USD', '{"max_athletes":50}', 1, 14, 1, 5),
(@plan_school,       NOW(6), 'SCHOOL',             'School',                  'ORG_ADMIN',29.99,'USD','{"max_athletes":100,"max_org_members":20}', 1, 30, 1, 6),
(@plan_academy,      NOW(6), 'ACADEMY',            'Academy',                 'ORG_ADMIN',49.99,'USD','{"max_athletes":300,"max_org_members":50,"SHARED_SCOUTING_WORKSPACE":1}', 1, 30, 1, 7),
(@plan_club,         NOW(6), 'CLUB',               'Club',                    'ORG_ADMIN',79.99,'USD','{"max_athletes":500,"max_org_members":100,"SHARED_SCOUTING_WORKSPACE":1,"MAX_SCOUT_SEATS":5}', 1, 30, 1, 8),
(@plan_scouting_agency,NOW(6),'SCOUTING_AGENCY',  'Scouting Agency',         'ORG_ADMIN',99.99,'USD','{"MAX_WATCHLISTS":100,"SCOUT_EVALUATIONS":1,"ADVANCED_ATHLETE_SEARCH":1,"MAX_SCOUT_SEATS":20}', 1, 30, 1, 9),
(@plan_federation,   NOW(6), 'FEDERATION',         'Federation / National Team','ORG_ADMIN',199.99,'USD','{"max_athletes":2000,"max_org_members":500,"SHARED_SCOUTING_WORKSPACE":1}', 1, 30, 0, 10),
(@plan_enterprise,   NOW(6), 'ENTERPRISE',         'Enterprise',              'ORG_ADMIN',0.00, 'USD', '{"max_athletes":9999999,"unlimited":1}', 1, 0, 0, 11);

-- NGN prices
INSERT INTO subscription_plan_prices (id, created_at, plan_id, currency, amount, billing_period, active) VALUES
(UUID(), NOW(6), @plan_guardian,     'NGN', 7500.00,  'MONTHLY', 1),
(UUID(), NOW(6), @plan_scout_ind,    'NGN', 14999.00, 'MONTHLY', 1),
(UUID(), NOW(6), @plan_scout_pro,    'NGN', 29999.00, 'MONTHLY', 1),
(UUID(), NOW(6), @plan_coach_agent,  'NGN', 22499.00, 'MONTHLY', 1),
(UUID(), NOW(6), @plan_school,       'NGN', 44999.00, 'MONTHLY', 1),
(UUID(), NOW(6), @plan_academy,      'NGN', 74999.00, 'MONTHLY', 1),
(UUID(), NOW(6), @plan_club,         'NGN',119999.00, 'MONTHLY', 1);

-- USD prices (MONTHLY)
INSERT INTO subscription_plan_prices (id, created_at, plan_id, currency, amount, billing_period, active) VALUES
(UUID(), NOW(6), @plan_guardian,     'USD', 4.99,   'MONTHLY', 1),
(UUID(), NOW(6), @plan_scout_ind,    'USD', 9.99,   'MONTHLY', 1),
(UUID(), NOW(6), @plan_scout_pro,    'USD', 19.99,  'MONTHLY', 1),
(UUID(), NOW(6), @plan_coach_agent,  'USD', 14.99,  'MONTHLY', 1),
(UUID(), NOW(6), @plan_school,       'USD', 29.99,  'MONTHLY', 1),
(UUID(), NOW(6), @plan_academy,      'USD', 49.99,  'MONTHLY', 1),
(UUID(), NOW(6), @plan_club,         'USD', 79.99,  'MONTHLY', 1);
