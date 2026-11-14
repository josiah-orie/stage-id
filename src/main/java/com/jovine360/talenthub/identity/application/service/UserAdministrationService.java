package com.jovine360.talenthub.identity.application.service;

import com.jovine360.talenthub.identity.api.AdminUserDto;
import com.jovine360.talenthub.identity.api.UserAdministrationApi;
import com.jovine360.talenthub.identity.domain.model.User;
import com.jovine360.talenthub.identity.domain.model.UserRole;
import com.jovine360.talenthub.identity.domain.model.UserStatus;
import com.jovine360.talenthub.identity.domain.repository.RefreshTokenRepository;
import com.jovine360.talenthub.identity.domain.repository.UserRepository;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@Transactional
public class UserAdministrationService implements UserAdministrationApi {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdministrationService(UserRepository userRepository,
                                     RefreshTokenRepository refreshTokenRepository,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserDto> listUsers(String role, String status, Pageable pageable) {
        UserRole r = role == null || role.isBlank() ? null : UserRole.valueOf(role.toUpperCase());
        UserStatus s = status == null || status.isBlank() ? null : UserStatus.valueOf(status.toUpperCase());
        return userRepository.search(r, s, pageable).map(UserAdministrationService::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDto getUser(String userId) {
        return toDto(load(userId));
    }

    @Override
    public AdminUserDto suspend(String targetUserId, String actorId, Collection<String> actorRoles) {
        User user = load(targetUserId);
        assertMayActOn(user, actorId, actorRoles, "suspend");
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidStateTransitionException("User", "SUSPENDED", "SUSPENDED");
        }
        user.setStatus(UserStatus.SUSPENDED);
        refreshTokenRepository.revokeAllByUser(user);
        return toDto(userRepository.save(user));
    }

    @Override
    public AdminUserDto reactivate(String targetUserId, String actorId, Collection<String> actorRoles) {
        User user = load(targetUserId);
        assertMayActOn(user, actorId, actorRoles, "reactivate");
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new InvalidStateTransitionException("User", "ACTIVE", "ACTIVE");
        }
        user.setStatus(UserStatus.ACTIVE);
        user.resetFailedAttempts();
        return toDto(userRepository.save(user));
    }

    @Override
    public AdminUserDto changePrimaryRole(String targetUserId, String newRole, String actorId) {
        User user = load(targetUserId);
        if (user.getId().equals(actorId)) {
            throw new IllegalArgumentException("You cannot change your own role");
        }
        UserRole role = UserRole.valueOf(newRole.toUpperCase());
        UserRole old = user.getRole();
        user.setRole(role);
        user.getAdditionalRoles().remove(role);
        if (old == UserRole.ADMIN || old == UserRole.SUPER_ADMIN) {
            // A demotion must not leave the privileged role behind as an additional role
            user.getAdditionalRoles().remove(UserRole.ADMIN);
            user.getAdditionalRoles().remove(UserRole.SUPER_ADMIN);
        }
        refreshTokenRepository.revokeAllByUser(user);
        return toDto(userRepository.save(user));
    }

    @Override
    public AdminUserDto createAdmin(String email, String password, String actorId) {
        String normalised = email.toLowerCase().trim();
        if (userRepository.existsByEmail(normalised)) {
            throw new DuplicateResourceException("User", "email", normalised);
        }
        User user = new User();
        user.setEmail(normalised);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        return toDto(userRepository.save(user));
    }

    // ---- helpers ---------------------------------------------------------------------------

    private static void assertMayActOn(User target, String actorId, Collection<String> actorRoles, String action) {
        if (target.getId().equals(actorId)) {
            throw new IllegalArgumentException("You cannot " + action + " your own account");
        }
        boolean targetPrivileged = target.hasRole(UserRole.ADMIN) || target.hasRole(UserRole.SUPER_ADMIN);
        boolean actorSuper = actorRoles != null && actorRoles.contains("SUPER_ADMIN");
        if (targetPrivileged && !actorSuper) {
            throw new SecurityException("Only a SUPER_ADMIN can " + action + " an administrator account");
        }
    }

    private User load(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User", userId));
    }

    static AdminUserDto toDto(User u) {
        return new AdminUserDto(u.getId(), u.getEmail(), u.getRole().name(),
                u.allRoles().stream().map(Enum::name).toList(), u.getStatus().name(),
                u.isEmailVerified(), u.getCreatedAt(), u.getLastLoginAt());
    }
}
