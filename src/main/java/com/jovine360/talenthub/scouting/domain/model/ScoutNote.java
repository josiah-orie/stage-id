package com.jovine360.talenthub.scouting.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "scout_notes")
@Getter
@Setter
public class ScoutNote extends AuditableEntity {

    @Column(name = "scout_profile_id", columnDefinition = "CHAR(36)", nullable = false)
    private String scoutProfileId;

    @Column(name = "athlete_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteId;

    @Column(name = "note_text", columnDefinition = "TEXT", nullable = false)
    private String noteText;

    @Column(name = "note_date")
    private LocalDate noteDate;

    @Column(name = "is_private", nullable = false)
    private boolean isPrivate = true;
}
