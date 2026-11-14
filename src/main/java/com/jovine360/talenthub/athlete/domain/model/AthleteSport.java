package com.jovine360.talenthub.athlete.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A secondary sport an athlete plays. The primary sport stays on the athlete profile. */
@Entity
@Table(name = "athlete_sports")
@Getter
@Setter
public class AthleteSport extends AuditableEntity {

    @Column(name = "athlete_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String athleteUserId;

    @Column(name = "sport_id", columnDefinition = "CHAR(36)", nullable = false)
    private String sportId;

    @Column(name = "sport_code", length = 10, nullable = false)
    private String sportCode;

    @Column(name = "position", length = 100)
    private String position;
}
