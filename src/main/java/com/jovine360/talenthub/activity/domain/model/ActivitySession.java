package com.jovine360.talenthub.activity.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Records a single training session or activity for an athlete.
 */
@Entity
@Table(name = "activity_sessions")
@Getter
@Setter
public class ActivitySession extends AuditableEntity {

    @Column(name = "athlete_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteUserId;

    @Column(name = "template_code", length = 30, nullable = false)
    private String templateCode;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "sport_code", length = 10)
    private String sportCode;

    /** JSON data matching the template schema. */
    @Column(name = "data_json", columnDefinition = "TEXT", nullable = false)
    private String dataJson;

    @Column(name = "coach_notes", columnDefinition = "TEXT")
    private String coachNotes;

    @Column(name = "logged_by", columnDefinition = "CHAR(36)")
    private String loggedBy;
}
