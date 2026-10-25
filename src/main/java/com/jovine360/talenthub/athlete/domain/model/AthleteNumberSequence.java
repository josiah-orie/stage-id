package com.jovine360.talenthub.athlete.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Sequence counter for athlete numbers per prefix.
 * Uses SELECT FOR UPDATE to generate contiguous, collision-free sequences.
 *
 * <p>One row per prefix (e.g. "TH", "FCB-FB").
 */
@Entity
@Table(name = "athlete_number_sequences")
@Getter
@Setter
public class AthleteNumberSequence extends BaseEntity {

    /** Prefix key e.g. "TH" for unaffiliated, "FCB-FB" for org+sport. */
    @Column(name = "prefix_key", length = 30, nullable = false, unique = true)
    private String prefixKey;

    @Column(name = "last_seq", nullable = false)
    private long lastSeq = 0L;
}
