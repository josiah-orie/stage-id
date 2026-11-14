package com.jovine360.talenthub.organization.api;

/** Published when an ACTIVE affiliation ends. */
public record OrganizationAthleteAffiliationEndedEvent(String relationshipId, String organizationId,
                                                       String athleteUserId) {}
