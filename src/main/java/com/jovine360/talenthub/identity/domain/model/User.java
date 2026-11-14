package com.jovine360.talenthub.identity.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Core user account entity — stores authentication credentials and role.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends AuditableEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 72)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    /** Roles held in addition to the primary {@link #role}. Stored in {@code user_roles}. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, nullable = false)
    private Set<UserRole> additionalRoles = new LinkedHashSet<>();

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 100)
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expires_at")
    private Instant emailVerificationTokenExpiresAt;

    @Column(name = "password_reset_token", length = 100)
    private String passwordResetToken;

    @Column(name = "password_reset_token_expires_at")
    private Instant passwordResetTokenExpiresAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    /** The primary role followed by any additional roles, without duplicates. */
    public List<UserRole> allRoles() {
        Set<UserRole> all = new LinkedHashSet<>();
        all.add(role);
        all.addAll(additionalRoles);
        return List.copyOf(all);
    }

    public boolean hasRole(UserRole r) {
        return role == r || additionalRoles.contains(r);
    }

    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    public void incrementFailedAttempts() {
        failedLoginAttempts++;
        if (failedLoginAttempts >= 5) {
            lockedUntil = Instant.now().plusSeconds(15 * 60); // 15-minute lockout
        }
    }

    public void resetFailedAttempts() {
        failedLoginAttempts = 0;
        lockedUntil = null;
    }
}
