-- V28: Media moderation status. CLEAN by default; FLAGGED when reported; RESTRICTED by an admin.
ALTER TABLE media_assets
    ADD COLUMN moderation_status VARCHAR(20)  NOT NULL DEFAULT 'CLEAN',
    ADD COLUMN moderation_note   VARCHAR(500) NULL,
    ADD COLUMN moderated_by      CHAR(36)     NULL,
    ADD COLUMN moderated_at      DATETIME(6)  NULL;

CREATE INDEX idx_media_moderation ON media_assets (moderation_status);
