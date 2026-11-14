package com.jovine360.talenthub.organization.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** @param permission VIEW_PROFILE, VIEW_STATS, VIEW_DOCUMENTS, CONTACT or MANAGE (default VIEW_STATS) */
public record RequestAffiliationRequest(@NotBlank String athleteId, @Size(max = 30) String permission) {}
