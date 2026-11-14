package com.jovine360.talenthub.guardian.domain.model;

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
 * Links a guardian user to a minor athlete.
 * Enforces safeguarding rules for athletes under 18.
 */
@Entity
@Table(name = "guardian_links")
@Getter
@Setter
public class GuardianLink extends AuditableEntity {

    @Column(name = "guardian_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String guardianUserId;

    @Column(name = "athlete_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteUserId;

    @Column(name = "relationship", length = 50)
    private String relationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private GuardianLinkStatus status = GuardianLinkStatus.PENDING;

    @Column(name = "consent_given", nullable = false)
    private boolean consentGiven = false;

    @Column(name = "consent_given_at")
    private Instant consentGivenAt;

    @Column(name = "verified_by", columnDefinition = "CHAR(36)")
    private String verifiedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", length = 30, nullable = false)
    private GuardianRelationshipType relationshipType = GuardianRelationshipType.PARENT;

    /** Access level an AGENT is requesting (an AthletePermission name). Null for other types. */
    @Column(name = "permission", length = 30)
    private String permission;

    @Column(name = "verification_document_ref", length = 255)
    private String verificationDocumentRef;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /** ACTIVE, consented and not expired. */
    public boolean isEffectivelyActive() {
        return status == GuardianLinkStatus.ACTIVE && consentGiven && !isExpired();
    }

    /** PARENT or LEGAL_GUARDIAN: full management of the athlete (AGENT links use access grants). */
    public boolean isGuardianLevel() {
        return relationshipType != GuardianRelationshipType.AGENT;
    }
}
