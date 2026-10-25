package com.jovine360.talenthub.sport.domain.repository;

import com.jovine360.talenthub.sport.domain.model.Sport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SportRepository extends JpaRepository<Sport, String> {
    Optional<Sport> findByCode(String code);
    List<Sport> findByActiveTrue();
    boolean existsByCode(String code);
}
