package com.jovine360.talenthub.scouting.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "scout_favourites",
       uniqueConstraints = @UniqueConstraint(columnNames = {"scout_profile_id", "athlete_id"}))
@Getter
@Setter
public class ScoutFavourite extends BaseEntity {

    @Column(name = "scout_profile_id", columnDefinition = "CHAR(36)", nullable = false)
    private String scoutProfileId;

    @Column(name = "athlete_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteId;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
