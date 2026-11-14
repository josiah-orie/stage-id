-- V1001 (dev only): every seeded organization's admin becomes its OWNER member.
-- On a fresh dev DB V1000 runs after V27, so V27's own backfill finds no organizations yet.
INSERT INTO organization_members (id, created_at, organization_id, user_id, member_role, status, joined_at)
SELECT UUID(), NOW(6), o.id, o.admin_user_id, 'OWNER', 'ACTIVE', NOW(6)
  FROM organizations o
 WHERE NOT EXISTS (SELECT 1 FROM organization_members m
                    WHERE m.organization_id = o.id AND m.user_id = o.admin_user_id);
