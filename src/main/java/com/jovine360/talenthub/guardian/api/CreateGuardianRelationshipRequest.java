package com.jovine360.talenthub.guardian.api;

import com.jovine360.talenthub.guardian.domain.model.GuardianRelationshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @param athleteId                the athlete's user id
 * @param relationshipType         PARENT, LEGAL_GUARDIAN or AGENT
 * @param relationship             optional free-text label, such as "Mother"
 * @param verificationDocumentRef  media asset id of the proof document. Required for LEGAL_GUARDIAN
 * @param permission               AGENT only: requested access level (VIEW_PROFILE, VIEW_STATS,
 *                                 VIEW_DOCUMENTS, CONTACT, MANAGE). Defaults to VIEW_STATS
 */
public record CreateGuardianRelationshipRequest(
        @NotBlank String athleteId,
        @NotNull GuardianRelationshipType relationshipType,
        @Size(max = 50) String relationship,
        @Size(max = 255) String verificationDocumentRef,
        @Size(max = 30) String permission
) {}
