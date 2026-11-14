package com.jovine360.talenthub.organization.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "organization_invitations")
@Getter
@Setter
public class OrganizationInvitation extends AuditableEntity {

    @Column(name = "organization_id", columnDefinition = "CHAR(36)", nullable = false)
    private String organizationId;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", length = 30, nullable = false)
    private OrgMemberRole memberRole;

    @Column(name = "token", length = 100, nullable = false, unique = true)
    private String token;

    /** PENDING, ACCEPTED, REVOKED. */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING";

    @Column(name = "invited_by", columnDefinition = "CHAR(36)", nullable = false)
    private String invitedBy;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_by", columnDefinition = "CHAR(36)")
    private String acceptedBy;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
