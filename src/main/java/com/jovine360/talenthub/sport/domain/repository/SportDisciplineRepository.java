package com.jovine360.talenthub.sport.domain.repository;

import com.jovine360.talenthub.sport.domain.model.SportDiscipline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SportDisciplineRepository extends JpaRepository<SportDiscipline, String> {
    List<SportDiscipline> findBySportIdOrderByName(String sportId);
    boolean existsBySportIdAndCode(String sportId, String code);
}
