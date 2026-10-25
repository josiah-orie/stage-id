package com.jovine360.talenthub.identity.api;

import com.jovine360.talenthub.identity.application.command.LoginCommand;
import com.jovine360.talenthub.identity.application.command.RefreshTokenCommand;
import com.jovine360.talenthub.identity.application.command.RegisterUserCommand;
import com.jovine360.talenthub.identity.application.service.AuthService;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

/**
 * Authentication REST endpoints.
 * All paths are public (see {@link com.jovine360.talenthub.shared.infrastructure.configuration.SecurityConfig}).
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register, login, token refresh, password reset")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new user account")
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String userId = authService.register(
                new RegisterUserCommand(request.email(), request.password(), request.role()));
        return ResponseEntity
                .created(URI.create("/api/v1/auth/me"))
                .body(Map.of("userId", userId,
                        "message", "Registration successful. Please verify your email."));
    }

    @Operation(summary = "Log in and receive JWT tokens")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(
                new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token using a refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(
                new RefreshTokenCommand(request.refreshToken()));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Log out — revokes all refresh tokens")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        authService.logout(principal.userId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Verify email address using the token sent to email")
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
    }

    @Operation(summary = "Request a password reset email")
    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        authService.initiatePasswordReset(request.email());
        // Always return 200 to prevent user enumeration
        return ResponseEntity.ok(Map.of("message",
                "If that email is registered, a reset link has been sent."));
    }

    @Operation(summary = "Complete password reset using the reset token")
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Map<String, String>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successful."));
    }
}
