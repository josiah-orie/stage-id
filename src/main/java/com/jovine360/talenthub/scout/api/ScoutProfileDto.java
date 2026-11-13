package com.jovine360.talenthub.scout.api;

public record ScoutProfileDto(
        String id,
        String userAccountId,
        String professionalTitle,
        String biography,
        Integer yearsExperience,
        String currentOrganizationId,
        String verificationStatus
) {}
