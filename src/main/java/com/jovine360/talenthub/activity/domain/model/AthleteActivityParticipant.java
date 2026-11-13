package com.jovine360.talenthub.activity.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "athlete_activity_participants")
@Getter
@Setter
public class AthleteActivityParticipant extends AuditableEntity {

    @Column(name = "activity_id", columnDefinition = "CHAR(36)", nullable = false)
    private String activityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_type", length = 20, nullable = false)
    private ParticipantType participantType;

    @Column(name = "participant_id", columnDefinition = "CHAR(36)")
    private String participantId;

    @Column(name = "participant_name", length = 200)
    private String participantName;
}
