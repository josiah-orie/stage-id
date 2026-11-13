package com.jovine360.talenthub.scout.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "scout_profiles")
@Getter
@Setter
public class ScoutProfile extends AuditableEntity {

    @Column(name = "user_account_id", columnDefinition = "CHAR(36)", nullable = false, unique = true)
    private String userAccountId;

    @Column(name = "professional_title", length = 150)
    private String professionalTitle;

    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "current_organization_id", columnDefinition = "CHAR(36)")
    private String currentOrganizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20, nullable = false)
    private ScoutVerificationStatus verificationStatus = ScoutVerificationStatus.UNVERIFIED;
}
