package com.jovine360.talenthub.sport.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sport_disciplines")
@Getter
@Setter
public class SportDiscipline extends AuditableEntity {

    @Column(name = "sport_id", columnDefinition = "CHAR(36)", nullable = false)
    private String sportId;

    @Column(name = "code", length = 30, nullable = false)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
