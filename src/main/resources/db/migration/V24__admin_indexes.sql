-- Add performance indexes for admin audit log queries
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_action ON audit_logs (actor_id, action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs (created_at);
