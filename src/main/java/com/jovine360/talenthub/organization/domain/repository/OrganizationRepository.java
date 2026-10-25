package com.jovine360.talenthub.organization.domain.repository;

import com.jovine360.talenthub.organization.domain.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, String> {
    Optional<Organization> findByCode(String code);
    boolean existsByCode(String code);
    List<Organization> findByAdminUserId(String adminUserId);
}
