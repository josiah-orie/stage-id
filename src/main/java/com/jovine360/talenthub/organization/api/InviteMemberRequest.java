package com.jovine360.talenthub.organization.api;

import com.jovine360.talenthub.organization.domain.model.OrgMemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(@Email @NotBlank String email, @NotNull OrgMemberRole memberRole) {}
