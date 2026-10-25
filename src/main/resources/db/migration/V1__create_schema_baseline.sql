-- V1: Schema baseline — creates the audit_logs table used across all modules.
-- All subsequent migrations build on top of this.

CREATE TABLE IF NOT EXISTS audit_logs (
    id            CHAR(36)     NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NULL,
    actor_id      CHAR(36)     NULL        COMMENT 'User who triggered the action (NULL = system)',
    actor_role    VARCHAR(50)  NULL,
    action        VARCHAR(100) NOT NULL    COMMENT 'e.g. USER_REGISTERED, ATHLETE_PROFILE_UPDATED',
    entity_type   VARCHAR(100) NOT NULL    COMMENT 'Fully-qualified class name or short entity name',
    entity_id     VARCHAR(36)  NULL        COMMENT 'PK of the affected entity',
    before_state  LONGTEXT     NULL        COMMENT 'JSON snapshot before change',
    after_state   LONGTEXT     NULL        COMMENT 'JSON snapshot after change',
    ip_address    VARCHAR(45)  NULL,
    user_agent    VARCHAR(512) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index added to support admin reporting queries by actor and action
CREATE INDEX idx_audit_logs_actor_id   ON audit_logs (actor_id);
CREATE INDEX idx_audit_logs_action     ON audit_logs (action);
CREATE INDEX idx_audit_logs_entity     ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
