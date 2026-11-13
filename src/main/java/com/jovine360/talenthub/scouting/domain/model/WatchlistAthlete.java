package com.jovine360.talenthub.scouting.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "watchlist_athletes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"watchlist_id", "athlete_id"}))
@Getter
@Setter
public class WatchlistAthlete extends BaseEntity {

    @Column(name = "watchlist_id", columnDefinition = "CHAR(36)", nullable = false)
    private String watchlistId;

    @Column(name = "athlete_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteId;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @Column(name = "added_by", columnDefinition = "CHAR(36)")
    private String addedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
