package com.jovine360.talenthub.organization.domain.repository;

import com.jovine360.talenthub.organization.domain.model.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, String> {
    List<OrganizationMember> findByOrganizationIdAndStatus(String organizationId, String status);
    List<OrganizationMember> findByUserIdAndStatus(String userId, String status);
    Optional<OrganizationMember> findByOrganizationIdAndUserIdAndStatus(String organizationId, String userId, String status);
}
