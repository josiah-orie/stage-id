package com.jovine360.talenthub.media.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "media_variants")
@Getter
@Setter
public class MediaVariant extends BaseEntity {

    @Column(name = "media_asset_id", columnDefinition = "CHAR(36)", nullable = false)
    private String mediaAssetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "variant_type", length = 20, nullable = false)
    private VariantType variantType;

    @Column(name = "object_key", length = 1000)
    private String objectKey;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
