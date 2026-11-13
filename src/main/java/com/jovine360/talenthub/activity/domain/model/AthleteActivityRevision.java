package com.jovine360.talenthub.activity.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "athlete_activity_revisions")
@Getter
@Setter
public class AthleteActivityRevision extends BaseEntity {

    @Column(name = "activity_id", columnDefinition = "CHAR(36)", nullable = false)
    private String activityId;

    @Column(name = "revision_number", nullable = false)
    private int revisionNumber;

    @Column(name = "changed_by", columnDefinition = "CHAR(36)", nullable = false)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "change_summary", length = 500)
    private String changeSummary;

    @Column(name = "snapshot_json", columnDefinition = "TEXT")
    private String snapshotJson;
}
