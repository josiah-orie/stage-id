package com.jovine360.talenthub.organization.api;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(@NotBlank String invitationToken) {}
