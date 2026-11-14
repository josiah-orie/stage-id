package com.jovine360.talenthub.identity.application.service;

import com.jovine360.talenthub.identity.api.AuthResponse;
import com.jovine360.talenthub.identity.application.command.LoginCommand;
import com.jovine360.talenthub.identity.application.command.RefreshTokenCommand;
import com.jovine360.talenthub.identity.application.command.RegisterUserCommand;
import com.jovine360.talenthub.identity.domain.event.UserRegisteredEvent;
import com.jovine360.talenthub.identity.domain.exception.AccountLockedException;
import com.jovine360.talenthub.identity.domain.exception.AccountSuspendedException;
import com.jovine360.talenthub.identity.domain.exception.InvalidCredentialsException;
import com.jovine360.talenthub.identity.domain.model.RefreshToken;
import com.jovine360.talenthub.identity.domain.model.User;
import com.jovine360.talenthub.identity.domain.model.UserStatus;
import com.jovine360.talenthub.identity.domain.repository.RefreshTokenRepository;
import com.jovine360.talenthub.identity.domain.repository.UserRepository;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.service.JwtTokenService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Core authentication service — registration, login, token refresh, logout.
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final ApplicationEventPublisher eventPublisher;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Registers a new user account and publishes {@link UserRegisteredEvent}.
     */
    public String register(RegisterUserCommand cmd) {
        if (cmd.role().isPrivileged()) {
            throw new IllegalArgumentException(
                    "Role " + cmd.role() + " cannot be self-assigned at registration");
        }
        if (userRepository.existsByEmail(cmd.email())) {
            throw new DuplicateResourceException("User", "email", cmd.email());
        }

        User user = new User();
        user.setEmail(cmd.email().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(cmd.password()));
        user.setRole(cmd.role());
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationTokenExpiresAt(Instant.now().plusSeconds(24 * 3600));

        user = userRepository.save(user);

        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail(), user.getRole()));

        return user.getId();
    }

    /**
     * Authenticates a user and returns access + refresh tokens.
     */
    public AuthResponse login(LoginCommand cmd) {
        User user = userRepository.findByEmail(cmd.email().toLowerCase().trim())
                .orElseThrow(InvalidCredentialsException::new);

        if (user.isLocked()) {
            throw new AccountLockedException();
        }

        if (!passwordEncoder.matches(cmd.password(), user.getPasswordHash())) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            throw new InvalidCredentialsException();
        }

        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DEACTIVATED) {
            throw new AccountSuspendedException();
        }
        user.resetFailedAttempts();
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String accessToken = jwtTokenService.generateAccessToken(
                user.getId(), user.getEmail(), roleNames(user));

        String rawRefreshToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(passwordEncoder.encode(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plusSeconds(
                jwtTokenService.getRefreshTokenExpiryDays() * 24 * 3600));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, rawRefreshToken, user.getId(),
                user.getEmail(), user.getRole().name(),
                jwtTokenService.getAccessTokenExpiryMinutes() * 60);
    }

    /**
     * Exchanges a valid refresh token for new access + refresh tokens (token rotation).
     */
    public AuthResponse refresh(RefreshTokenCommand cmd) {
        // Find active refresh tokens and match the hash
        // We search by userId embedded in tokens; for efficiency we load candidate tokens
        List<RefreshToken> candidates = refreshTokenRepository.findAll();
        RefreshToken matched = candidates.stream()
                .filter(rt -> !rt.isRevoked() && !rt.isExpired())
                .filter(rt -> passwordEncoder.matches(cmd.refreshToken(), rt.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new InvalidCredentialsException());

        // Revoke the used refresh token (rotation)
        matched.setRevoked(true);
        refreshTokenRepository.save(matched);

        User user = matched.getUser();
        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DEACTIVATED) {
            throw new AccountSuspendedException();
        }
        String accessToken = jwtTokenService.generateAccessToken(
                user.getId(), user.getEmail(), roleNames(user));

        String rawRefreshToken = UUID.randomUUID().toString();
        RefreshToken newToken = new RefreshToken();
        newToken.setUser(user);
        newToken.setTokenHash(passwordEncoder.encode(rawRefreshToken));
        newToken.setExpiresAt(Instant.now().plusSeconds(
                jwtTokenService.getRefreshTokenExpiryDays() * 24 * 3600));
        refreshTokenRepository.save(newToken);

        return new AuthResponse(accessToken, rawRefreshToken, user.getId(),
                user.getEmail(), user.getRole().name(),
                jwtTokenService.getAccessTokenExpiryMinutes() * 60);
    }

    /**
     * Revokes all refresh tokens for the given user (logout all sessions).
     */
    public void logout(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        refreshTokenRepository.revokeAllByUser(user);
    }

    /**
     * Verifies an email address using the token sent at registration.
     */
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Verification token not found or expired"));

        if (user.getEmailVerificationTokenExpiresAt() != null
                && Instant.now().isAfter(user.getEmailVerificationTokenExpiresAt())) {
            throw new IllegalArgumentException("Email verification token has expired");
        }

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiresAt(null);
        userRepository.save(user);
    }

    /**
     * Initiates password reset — generates and stores a reset token.
     * Returns the raw token for testing; in production, email delivery handles distribution.
     */
    public String initiatePasswordReset(String email) {
        // Return quietly if user not found to prevent enumeration
        User user = userRepository.findByEmail(email.toLowerCase().trim()).orElse(null);
        if (user == null) return null;

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiresAt(Instant.now().plusSeconds(3600)); // 1 hour
        userRepository.save(user);
        return token;
    }

    /**
     * Completes password reset using the token.
     */
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidCredentialsException());

        if (user.getPasswordResetTokenExpiresAt() == null
                || Instant.now().isAfter(user.getPasswordResetTokenExpiresAt())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        user.resetFailedAttempts();
        userRepository.save(user);

        // Revoke all refresh tokens after password change
        refreshTokenRepository.revokeAllByUser(user);
    }

    private static List<String> roleNames(User user) {
        return user.allRoles().stream().map(Enum::name).toList();
    }
}
