package com.jovine360.talenthub.organization.api;

import java.time.Instant;

/**
 * {@code invitationToken} is only included in the response to the inviter, who passes it to the
 * invitee. It is the secret used to accept.
 */
public record InvitationDto(
        String id, String organizationId, String email, String memberRole,
        String status, String invitationToken, Instant expiresAt
) {}
