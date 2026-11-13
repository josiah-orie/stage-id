-- V10: Activity configuration tables (full DDD model).

CREATE TABLE IF NOT EXISTS activity_types (
    id          CHAR(36)    NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    code        VARCHAR(30) NOT NULL,
    name        VARCHAR(100)NOT NULL,
    description TEXT        NULL,
    active      TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_activity_types_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS activity_templates (
    id               CHAR(36)    NOT NULL,
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NULL,
    created_by       CHAR(36)    NULL,
    updated_by       CHAR(36)    NULL,
    activity_type_id CHAR(36)    NOT NULL,
    sport_id         CHAR(36)    NULL,
    code             VARCHAR(30) NOT NULL,
    name             VARCHAR(100)NOT NULL,
    description      TEXT        NULL,
    sport_code       VARCHAR(10) NULL,
    active           TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_activity_templates_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS activity_schema_versions (
    id             CHAR(36)    NOT NULL,
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NULL,
    created_by     CHAR(36)    NULL,
    updated_by     CHAR(36)    NULL,
    template_id    CHAR(36)    NOT NULL,
    version_number INT         NOT NULL DEFAULT 1,
    status         VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    effective_date DATE        NULL,
    published_at   DATETIME(6) NULL,
    published_by   CHAR(36)    NULL,
    PRIMARY KEY (id),
    INDEX idx_schema_versions_template (template_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS activity_field_definitions (
    id                   CHAR(36)     NOT NULL,
    created_at           DATETIME(6)  NOT NULL,
    updated_at           DATETIME(6)  NULL,
    created_by           CHAR(36)     NULL,
    updated_by           CHAR(36)     NULL,
    schema_version_id    CHAR(36)     NOT NULL,
    field_code           VARCHAR(60)  NOT NULL,
    field_label          VARCHAR(120) NOT NULL,
    field_type           VARCHAR(30)  NOT NULL,
    required             TINYINT(1)   NOT NULL DEFAULT 0,
    display_order        INT          NOT NULL DEFAULT 0,
    validation_rules_json TEXT        NULL,
    help_text            TEXT         NULL,
    active               TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    INDEX idx_field_defs_schema (schema_version_id, display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS activity_field_options (
    id                  CHAR(36)    NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NULL,
    created_by          CHAR(36)    NULL,
    updated_by          CHAR(36)    NULL,
    field_definition_id CHAR(36)    NOT NULL,
    option_value        VARCHAR(100)NOT NULL,
    display_label       VARCHAR(150)NOT NULL,
    ordinal             INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_field_options_field (field_definition_id, ordinal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS activity_validation_rules (
    id                  CHAR(36)    NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NULL,
    created_by          CHAR(36)    NULL,
    updated_by          CHAR(36)    NULL,
    field_definition_id CHAR(36)    NOT NULL,
    rule_type           VARCHAR(20) NOT NULL,
    rule_value          VARCHAR(500)NULL,
    error_message       VARCHAR(300)NULL,
    PRIMARY KEY (id),
    INDEX idx_val_rules_field (field_definition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- Seed all 11 activity types ----------------------------------------
SET @match_id    = UUID();
SET @training_id = UUID();
SET @trial_id    = UUID();
SET @pt_id       = UUID();
SET @comp_id     = UUID();
SET @fitness_id  = UUID();
SET @skills_id   = UUID();
SET @camp_id     = UUID();
SET @showcase_id = UUID();
SET @combine_id  = UUID();
SET @other_id    = UUID();

INSERT INTO activity_types (id, created_at, code, name, description, active) VALUES
(@match_id,    NOW(6), 'MATCH',              'Match',                'Competitive match or game',            1),
(@training_id, NOW(6), 'TRAINING',           'Training',             'Team or group training session',       1),
(@trial_id,    NOW(6), 'TRIAL',              'Trial',                'Selection trial event',                1),
(@pt_id,       NOW(6), 'PERSONAL_TRAINING',  'Personal Training',    'One-on-one coaching or PT session',    1),
(@comp_id,     NOW(6), 'COMPETITION',        'Competition',          'Tournament or competition entry',      1),
(@fitness_id,  NOW(6), 'FITNESS_ASSESSMENT', 'Fitness Assessment',   'Physical fitness evaluation',          1),
(@skills_id,   NOW(6), 'SKILLS_ASSESSMENT',  'Skills Assessment',    'Technical skills evaluation',         1),
(@camp_id,     NOW(6), 'TRAINING_CAMP',      'Training Camp',        'Residential or intensive camp',        1),
(@showcase_id, NOW(6), 'SHOWCASE',           'Showcase',             'Athlete showcase event',               1),
(@combine_id,  NOW(6), 'COMBINE',            'Combine',              'Multi-drill combine testing event',    1),
(@other_id,    NOW(6), 'OTHER',              'Other',                'Any other activity type',              1);

-- ---- Seed Football MATCH template with schema ----------------------------
SET @fb_match_tpl_id  = UUID();
SET @fb_match_sv_id   = UUID();

INSERT INTO activity_templates (id, created_at, activity_type_id, sport_id, code, name, description, sport_code, active)
VALUES (@fb_match_tpl_id, NOW(6), @match_id, NULL, 'FOOTBALL_MATCH', 'Football Match', 'Football match performance', 'FB', 1);

INSERT INTO activity_schema_versions (id, created_at, template_id, version_number, status, effective_date, published_at, published_by)
VALUES (@fb_match_sv_id, NOW(6), @fb_match_tpl_id, 1, 'PUBLISHED', CURDATE(), NOW(6), 'system');

INSERT INTO activity_field_definitions (id, created_at, schema_version_id, field_code, field_label, field_type, required, display_order, active) VALUES
(UUID(), NOW(6), @fb_match_sv_id, 'competition',      'Competition',       'TEXT',         0, 1,  1),
(UUID(), NOW(6), @fb_match_sv_id, 'opponent',         'Opponent',          'TEXT',         1, 2,  1),
(UUID(), NOW(6), @fb_match_sv_id, 'matchDate',        'Match Date',        'DATE',         1, 3,  1),
(UUID(), NOW(6), @fb_match_sv_id, 'teamRepresented',  'Team Represented',  'TEXT',         0, 4,  1),
(UUID(), NOW(6), @fb_match_sv_id, 'playingPosition',  'Playing Position',  'TEXT',         0, 5,  1),
(UUID(), NOW(6), @fb_match_sv_id, 'minutesPlayed',    'Minutes Played',    'INTEGER',      1, 6,  1),
(UUID(), NOW(6), @fb_match_sv_id, 'goals',            'Goals',             'INTEGER',      0, 7,  1),
(UUID(), NOW(6), @fb_match_sv_id, 'assists',          'Assists',           'INTEGER',      0, 8,  1),
(UUID(), NOW(6), @fb_match_sv_id, 'matchResult',      'Match Result',      'SINGLE_SELECT',0, 9,  1),
(UUID(), NOW(6), @fb_match_sv_id, 'performanceNotes', 'Performance Notes', 'LONG_TEXT',    0, 10, 1);

-- ---- Seed American Football COMBINE template ----------------------------
SET @af_combine_tpl_id = UUID();
SET @af_combine_sv_id  = UUID();

INSERT INTO activity_templates (id, created_at, activity_type_id, sport_id, code, name, description, sport_code, active)
VALUES (@af_combine_tpl_id, NOW(6), @combine_id, NULL, 'AMFOOTBALL_COMBINE', 'American Football Combine', 'Combine testing for American Football', 'AF', 1);

INSERT INTO activity_schema_versions (id, created_at, template_id, version_number, status, effective_date, published_at, published_by)
VALUES (@af_combine_sv_id, NOW(6), @af_combine_tpl_id, 1, 'PUBLISHED', CURDATE(), NOW(6), 'system');

INSERT INTO activity_field_definitions (id, created_at, schema_version_id, field_code, field_label, field_type, required, display_order, active) VALUES
(UUID(), NOW(6), @af_combine_sv_id, 'combineOrganizer', 'Combine Organizer', 'TEXT',    0, 1, 1),
(UUID(), NOW(6), @af_combine_sv_id, 'venue',            'Venue',             'TEXT',    0, 2, 1),
(UUID(), NOW(6), @af_combine_sv_id, 'position',         'Position',          'TEXT',    1, 3, 1),
(UUID(), NOW(6), @af_combine_sv_id, 'fortyYardDash',    '40-Yard Dash (s)',  'DECIMAL', 0, 4, 1),
(UUID(), NOW(6), @af_combine_sv_id, 'verticalJump',     'Vertical Jump (in)','DECIMAL', 0, 5, 1),
(UUID(), NOW(6), @af_combine_sv_id, 'broadJump',        'Broad Jump (in)',   'DECIMAL', 0, 6, 1),
(UUID(), NOW(6), @af_combine_sv_id, 'shuttleTime',      'Shuttle Time (s)',  'DECIMAL', 0, 7, 1),
(UUID(), NOW(6), @af_combine_sv_id, 'drillResults',     'Drill Results',     'LONG_TEXT',0,8, 1);

-- ---- Seed PERSONAL_TRAINING template ------------------------------------
SET @pt_tpl_id = UUID();
SET @pt_sv_id  = UUID();

INSERT INTO activity_templates (id, created_at, activity_type_id, sport_id, code, name, description, sport_code, active)
VALUES (@pt_tpl_id, NOW(6), @pt_id, NULL, 'PERSONAL_TRAINING_SESSION', 'Personal Training Session', 'One-on-one training session', NULL, 1);

INSERT INTO activity_schema_versions (id, created_at, template_id, version_number, status, effective_date, published_at, published_by)
VALUES (@pt_sv_id, NOW(6), @pt_tpl_id, 1, 'PUBLISHED', CURDATE(), NOW(6), 'system');

INSERT INTO activity_field_definitions (id, created_at, schema_version_id, field_code, field_label, field_type, required, display_order, active) VALUES
(UUID(), NOW(6), @pt_sv_id, 'trainingObjective',  'Training Objective',  'TEXT',     1, 1, 1),
(UUID(), NOW(6), @pt_sv_id, 'trainingCategory',   'Training Category',   'TEXT',     0, 2, 1),
(UUID(), NOW(6), @pt_sv_id, 'duration',           'Duration (min)',       'INTEGER',  1, 3, 1),
(UUID(), NOW(6), @pt_sv_id, 'exercisesPerformed', 'Exercises Performed', 'LONG_TEXT',0, 4, 1),
(UUID(), NOW(6), @pt_sv_id, 'skillsPractised',    'Skills Practised',    'LONG_TEXT',0, 5, 1),
(UUID(), NOW(6), @pt_sv_id, 'coachTrainer',       'Coach / Trainer',     'TEXT',     0, 6, 1),
(UUID(), NOW(6), @pt_sv_id, 'performanceNotes',   'Performance Notes',   'LONG_TEXT',0, 7, 1);
