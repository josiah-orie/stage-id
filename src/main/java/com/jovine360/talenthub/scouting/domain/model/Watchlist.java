package com.jovine360.talenthub.scouting.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "watchlists")
@Getter
@Setter
public class Watchlist extends AuditableEntity {

    @Column(name = "owner_id", columnDefinition = "CHAR(36)", nullable = false)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", length = 20, nullable = false)
    private OwnerType ownerType;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_shared", nullable = false)
    private boolean shared;
}
