-- V4: Sport configuration tables with 10 seeded sports.

CREATE TABLE IF NOT EXISTS sports (
    id          CHAR(36)     NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NULL,
    created_by  CHAR(36)     NULL,
    updated_by  CHAR(36)     NULL,
    code        VARCHAR(10)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    active      TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_sports_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sports_active ON sports (active);

-- Seed 10 sports
INSERT INTO sports (id, created_at, code, name, description, active) VALUES
(UUID(), NOW(6), 'FB',  'Football',        'Association football (soccer)', 1),
(UUID(), NOW(6), 'BK',  'Basketball',      'Basketball', 1),
(UUID(), NOW(6), 'AT',  'Athletics',       'Track and field athletics', 1),
(UUID(), NOW(6), 'SW',  'Swimming',        'Competitive swimming', 1),
(UUID(), NOW(6), 'TN',  'Tennis',          'Lawn tennis', 1),
(UUID(), NOW(6), 'RG',  'Rugby',           'Rugby union and rugby league', 1),
(UUID(), NOW(6), 'HK',  'Hockey',          'Field hockey', 1),
(UUID(), NOW(6), 'VB',  'Volleyball',      'Indoor and beach volleyball', 1),
(UUID(), NOW(6), 'BX',  'Boxing',          'Amateur and professional boxing', 1),
(UUID(), NOW(6), 'GF',  'Golf',            'Stroke play golf', 1);
