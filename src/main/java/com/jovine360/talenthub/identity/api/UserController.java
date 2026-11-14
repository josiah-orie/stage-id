package com.jovine360.talenthub.identity.api;

import com.jovine360.talenthub.identity.application.service.UserAccountService;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "The caller's own account")
public class UserController {

    private final UserAccountService accountService;

    public UserController(UserAccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Get the authenticated user's account")
    @GetMapping("/me")
    public ResponseEntity<UserAccountDto> me(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(accountService.getAccount(principal.userId()));
    }

    @Operation(summary = "Add a non-privileged role to the authenticated user's account")
    @PostMapping("/me/roles")
    public ResponseEntity<UserAccountDto> assignRole(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(accountService.assignRole(principal.userId(), request.role()));
    }
}
