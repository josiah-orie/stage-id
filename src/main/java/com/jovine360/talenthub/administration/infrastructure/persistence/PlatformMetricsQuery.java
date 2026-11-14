package com.jovine360.talenthub.administration.infrastructure.persistence;

import com.jovine360.talenthub.administration.api.PlatformDashboardResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only platform counts for the admin dashboard. A reporting query across module tables;
 * it never writes, so it does not go through each module's API.
 */
@Component
public class PlatformMetricsQuery {

    private final JdbcTemplate jdbc;

    public PlatformMetricsQuery(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private long count(String sql) {
        Long n = jdbc.queryForObject(sql, Long.class);
        return n == null ? 0 : n;
    }

    @Transactional(readOnly = true)
    public PlatformDashboardResponse load() {
        return new PlatformDashboardResponse(
                count("SELECT COUNT(*) FROM athlete_profiles"),
                count("SELECT COUNT(DISTINCT athlete_id) FROM athlete_activities WHERE verification_status = 'VERIFIED'"),
                count("SELECT COUNT(*) FROM scout_profiles"),
                count("SELECT COUNT(*) FROM scout_profiles WHERE verification_status = 'VERIFIED'"),
                count("SELECT COUNT(*) FROM organizations"),
                count("SELECT COUNT(*) FROM user_subscriptions WHERE status = 'ACTIVE'"),
                count("SELECT COUNT(*) FROM athlete_activities WHERE activity_status = 'PUBLISHED'"),
                count("SELECT COUNT(*) FROM media_assets WHERE asset_type = 'ACTIVITY_VIDEO' AND deleted_at IS NULL"),
                count("SELECT (SELECT COUNT(*) FROM scout_profiles WHERE verification_status = 'PENDING')"
                        + " + (SELECT COUNT(*) FROM organizations WHERE status = 'PENDING_VERIFICATION')"
                        + " + (SELECT COUNT(*) FROM guardian_links WHERE status = 'PENDING_ADMIN_VERIFICATION')"),
                count("SELECT (SELECT COUNT(*) FROM safeguarding_reports WHERE status = 'PENDING')"
                        + " + (SELECT COUNT(*) FROM media_assets WHERE moderation_status = 'FLAGGED')")
        );
    }
}
