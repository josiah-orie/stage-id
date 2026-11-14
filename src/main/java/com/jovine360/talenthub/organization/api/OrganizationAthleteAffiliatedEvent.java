package com.jovine360.talenthub.organization.api;

/** Published when an athlete approves an organization affiliation. */
public record OrganizationAthleteAffiliatedEvent(String relationshipId, String organizationId,
                                                 String athleteUserId, String permission) {}
