package com.jovine360.talenthub.organization.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * An organisation such as a club, school, or academy that can manage athletes.
 */
@Entity
@Table(name = "organizations")
@Getter
@Setter
public class Organization extends AuditableEntity {

    /** Short unique code used in athlete numbers e.g. "FCB". */
    @Column(name = "code", length = 10, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_type", length = 50, nullable = false)
    private OrgType orgType;

    @Column(name = "country_code", columnDefinition = "CHAR(2)")
    private String countryCode;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "admin_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String adminUserId;

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private OrgStatus status = OrgStatus.ACTIVE;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "verified_by", columnDefinition = "CHAR(36)")
    private String verifiedBy;

    @Column(name = "verified_at")
    private java.time.Instant verifiedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
}
