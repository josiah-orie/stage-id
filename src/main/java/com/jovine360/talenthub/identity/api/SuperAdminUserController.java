package com.jovine360.talenthub.identity.api;

import com.jovine360.talenthub.identity.domain.model.UserRole;
import com.jovine360.talenthub.shared.domain.service.AuditTrail;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Account operations reserved for SUPER_ADMIN. */
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Super admin: users", description = "Create administrators and change roles")
public class SuperAdminUserController {

    private final UserAdministrationApi userAdministration;
    private final AuditTrail audit;

    public SuperAdminUserController(UserAdministrationApi userAdministration, AuditTrail audit) {
        this.userAdministration = userAdministration;
        this.audit = audit;
    }

    public record CreateAdminRequest(@Email @NotBlank String email,
                                     @NotBlank @Size(min = 8, max = 128) String password) {}

    public record ChangeRoleRequest(@NotNull UserRole role) {}

    @Operation(summary = "Create an ADMIN account (active and email-verified)")
    @PostMapping
    public ResponseEntity<AdminUserDto> createAdmin(@Valid @RequestBody CreateAdminRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        AdminUserDto created = userAdministration.createAdmin(request.email(), request.password(), principal.userId());
        audit.record(principal.userId(), principal.roles(), "ADMIN_CREATED", "User", created.userId(), created.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Change a user's primary role (promote or demote). Revokes their refresh tokens")
    @PutMapping("/{id}/role")
    public ResponseEntity<AdminUserDto> changeRole(@PathVariable String id,
            @Valid @RequestBody ChangeRoleRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        AdminUserDto before = userAdministration.getUser(id);
        AdminUserDto after = userAdministration.changePrimaryRole(id, request.role().name(), principal.userId());
        audit.record(principal.userId(), principal.roles(), "USER_ROLE_CHANGED", "User", id,
                before.role() + " -> " + after.role());
        return ResponseEntity.ok(after);
    }
}
