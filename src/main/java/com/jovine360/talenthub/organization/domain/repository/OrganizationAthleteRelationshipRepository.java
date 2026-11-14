package com.jovine360.talenthub.organization.domain.repository;

import com.jovine360.talenthub.organization.domain.model.OrganizationAthleteRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrganizationAthleteRelationshipRepository extends JpaRepository<OrganizationAthleteRelationship, String> {
    List<OrganizationAthleteRelationship> findByOrganizationIdAndStatus(String organizationId, String status);
    List<OrganizationAthleteRelationship> findByAthleteUserIdOrderByCreatedAtDesc(String athleteUserId);
    List<OrganizationAthleteRelationship> findByOrganizationIdAndAthleteUserIdAndStatusIn(
            String organizationId, String athleteUserId, Collection<String> statuses);
}
