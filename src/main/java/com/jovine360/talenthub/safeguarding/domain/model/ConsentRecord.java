package com.jovine360.talenthub.safeguarding.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "consent_records")
@Getter
@Setter
public class ConsentRecord extends BaseEntity {

    @Column(name = "athlete_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose_code", length = 30, nullable = false)
    private ConsentPurpose purposeCode;

    @Column(name = "consent_version", length = 20)
    private String consentVersion;

    @Column(name = "consented_by", columnDefinition = "CHAR(36)", nullable = false)
    private String consentedBy;

    @Column(name = "consented_at", nullable = false)
    private Instant consentedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_by", columnDefinition = "CHAR(36)")
    private String revokedBy;

    @Column(name = "authorization_source", length = 20)
    private String authorizationSource; // SELF / GUARDIAN / ADMIN
}
