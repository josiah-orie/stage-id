package com.jovine360.talenthub.discovery.domain.repository;

import com.jovine360.talenthub.discovery.domain.model.SavedSearch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedSearchRepository extends JpaRepository<SavedSearch, String> {
    List<SavedSearch> findByScoutProfileId(String scoutProfileId);
}
