package com.jovine360.talenthub.scout.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "scout_specializations")
@Getter
@Setter
public class ScoutSpecialization extends AuditableEntity {

    @Column(name = "scout_profile_id", columnDefinition = "CHAR(36)", nullable = false)
    private String scoutProfileId;

    @Column(name = "sport_id", columnDefinition = "CHAR(36)", nullable = false)
    private String sportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 20)
    private ScoutingLevel level;

    @Column(name = "preferred_regions_json", columnDefinition = "TEXT")
    private String preferredRegionsJson;
}
