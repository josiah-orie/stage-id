package com.jovine360.talenthub.discovery.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "saved_searches")
@Getter
@Setter
public class SavedSearch extends BaseEntity {

    @Column(name = "scout_profile_id", columnDefinition = "CHAR(36)", nullable = false)
    private String scoutProfileId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "query_json", columnDefinition = "TEXT")
    private String queryJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
