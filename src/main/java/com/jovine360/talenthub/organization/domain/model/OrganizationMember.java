package com.jovine360.talenthub.organization.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "organization_members")
@Getter
@Setter
public class OrganizationMember extends AuditableEntity {

    @Column(name = "organization_id", columnDefinition = "CHAR(36)", nullable = false)
    private String organizationId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", length = 30, nullable = false)
    private OrgMemberRole memberRole;

    /** ACTIVE or REMOVED. */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "removed_at")
    private Instant removedAt;

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
