package com.jovine360.talenthub.identity.api;

import com.jovine360.talenthub.identity.domain.model.UserRole;
import jakarta.validation.constraints.NotNull;

public record AssignRoleRequest(@NotNull UserRole role) {}
