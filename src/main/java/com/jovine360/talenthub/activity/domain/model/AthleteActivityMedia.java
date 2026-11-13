package com.jovine360.talenthub.activity.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "athlete_activity_media")
@Getter
@Setter
public class AthleteActivityMedia extends AuditableEntity {

    @Column(name = "activity_id", columnDefinition = "CHAR(36)", nullable = false)
    private String activityId;

    @Column(name = "media_asset_id", columnDefinition = "CHAR(36)", nullable = false)
    private String mediaAssetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", length = 20, nullable = false)
    private MediaType mediaType;

    @Column(name = "caption", length = 300)
    private String caption;

    @Column(name = "ordinal", nullable = false)
    private int ordinal;
}
