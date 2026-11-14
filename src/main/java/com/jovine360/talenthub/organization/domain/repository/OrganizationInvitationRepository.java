package com.jovine360.talenthub.organization.domain.repository;

import com.jovine360.talenthub.organization.domain.model.OrganizationInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitation, String> {
    Optional<OrganizationInvitation> findByToken(String token);
    List<OrganizationInvitation> findByOrganizationIdAndStatus(String organizationId, String status);
}
