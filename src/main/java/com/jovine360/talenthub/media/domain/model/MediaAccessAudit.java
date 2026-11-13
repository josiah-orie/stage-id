package com.jovine360.talenthub.media.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "media_access_audits")
@Getter
@Setter
public class MediaAccessAudit extends BaseEntity {

    @Column(name = "media_asset_id", columnDefinition = "CHAR(36)", nullable = false)
    private String mediaAssetId;

    @Column(name = "accessed_by", columnDefinition = "CHAR(36)", nullable = false)
    private String accessedBy;

    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt;

    @Column(name = "access_type", length = 30, nullable = false)
    private String accessType;
}
