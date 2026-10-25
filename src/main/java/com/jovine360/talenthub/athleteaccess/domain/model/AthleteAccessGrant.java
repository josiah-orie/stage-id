package com.jovine360.talenthub.athleteaccess.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Records who has been granted what level of access to an athlete's profile.
 */
@Entity
@Table(name = "athlete_access_grants")
@Getter
@Setter
public class AthleteAccessGrant extends AuditableEntity {

    @Column(name = "athlete_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteUserId;

    @Column(name = "grantee_id", columnDefinition = "CHAR(36)", nullable = false)
    private String granteeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "grantee_type", length = 30, nullable = false)
    private GranteeType granteeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", length = 30, nullable = false)
    private AthletePermission permission;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return active && !isExpired();
    }
}
