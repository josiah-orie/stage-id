package com.jovine360.talenthub.administration;

import com.jovine360.talenthub.administration.application.service.AuditLogService;
import com.jovine360.talenthub.administration.domain.model.AuditLog;
import com.jovine360.talenthub.administration.domain.repository.AuditLogRepository;
import com.jovine360.talenthub.identity.application.command.LoginCommand;
import com.jovine360.talenthub.identity.application.service.AuthService;
import com.jovine360.talenthub.identity.application.service.UserAdministrationService;
import com.jovine360.talenthub.identity.domain.exception.AccountSuspendedException;
import com.jovine360.talenthub.identity.domain.model.User;
import com.jovine360.talenthub.identity.domain.model.UserRole;
import com.jovine360.talenthub.identity.domain.model.UserStatus;
import com.jovine360.talenthub.identity.domain.repository.RefreshTokenRepository;
import com.jovine360.talenthub.identity.domain.repository.UserRepository;
import com.jovine360.talenthub.shared.domain.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Admin account rules, suspended login, and audit-log scoping. No Spring context. */
class AdminUserAndAuditTest {

    private UserRepository users;
    private RefreshTokenRepository tokens;
    private UserAdministrationService admin;

    private User user(String id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setEmail(id + "@x.com");
        u.setPasswordHash("hash");
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        when(users.findById(id)).thenReturn(Optional.of(u));
        return u;
    }

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        tokens = mock(RefreshTokenRepository.class);
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        admin = new UserAdministrationService(users, tokens, mock(PasswordEncoder.class));
    }

    @Test
    void adminCanSuspendOrdinaryUserAndTokensAreRevoked() {
        User target = user("u1", UserRole.ATHLETE);
        var dto = admin.suspend("u1", "admin-1", List.of("ADMIN"));
        assertThat(dto.status()).isEqualTo("SUSPENDED");
        verify(tokens).revokeAllByUser(target);
    }

    @Test
    void adminCannotSuspendAnotherAdminButSuperAdminCan() {
        user("a2", UserRole.ADMIN);
        assertThatThrownBy(() -> admin.suspend("a2", "admin-1", List.of("ADMIN")))
                .isInstanceOf(SecurityException.class);
        assertThat(admin.suspend("a2", "super-1", List.of("SUPER_ADMIN")).status()).isEqualTo("SUSPENDED");
    }

    @Test
    void adminRoleHeldAsAdditionalRoleIsAlsoProtected() {
        User u = user("u3", UserRole.ATHLETE);
        u.getAdditionalRoles().add(UserRole.ADMIN);
        assertThatThrownBy(() -> admin.suspend("u3", "admin-1", List.of("ADMIN")))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void cannotSuspendYourself() {
        user("super-1", UserRole.SUPER_ADMIN);
        assertThatThrownBy(() -> admin.suspend("super-1", "super-1", List.of("SUPER_ADMIN")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void suspendedUserCannotLogIn() {
        User u = user("u1", UserRole.ATHLETE);
        u.setStatus(UserStatus.SUSPENDED);
        when(users.findByEmail("u1@x.com")).thenReturn(Optional.of(u));
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.matches("pw", "hash")).thenReturn(true);
        AuthService auth = new AuthService(users, tokens, encoder, mock(JwtTokenService.class),
                mock(ApplicationEventPublisher.class));

        assertThatThrownBy(() -> auth.login(new LoginCommand("u1@x.com", "pw")))
                .isInstanceOf(AccountSuspendedException.class);
    }

    @Test
    void auditLogIsScopedToTheCallerForAdminAndOpenForSuperAdmin() {
        AuditLogRepository repo = mock(AuditLogRepository.class);
        Page<AuditLog> empty = new PageImpl<>(List.of());
        when(repo.search(any(), any(), any(), any(), any(), any(), any())).thenReturn(empty);
        AuditLogService audit = new AuditLogService(repo);
        var page = PageRequest.of(0, 20);

        // ADMIN asking for someone else's actions still only gets their own
        audit.search("admin-1", List.of("ADMIN"), "admin-2", null, null, null, null, null, page);
        verify(repo).search(eq("admin-1"), isNull(), isNull(), isNull(), isNull(), isNull(), any());

        audit.search("super-1", List.of("SUPER_ADMIN"), "admin-2", null, null, null, null, null, page);
        verify(repo).search(eq("admin-2"), isNull(), isNull(), isNull(), isNull(), isNull(), any());

        audit.search("super-1", List.of("SUPER_ADMIN"), null, null, null, null, null, null, page);
        verify(repo).search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void auditRecordStoresMostPrivilegedRole() {
        AuditLogRepository repo = mock(AuditLogRepository.class);
        new AuditLogService(repo).record("u1", List.of("ATHLETE", "ADMIN"), "USER_SUSPENDED", "User", "u9", null);
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getActorRole()).isEqualTo("ADMIN");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void demotingAnAdminRemovesPrivilegedRolesAndRevokesTokens() {
        User u = user("a2", UserRole.ADMIN);
        u.getAdditionalRoles().add(UserRole.SUPER_ADMIN);
        u.getAdditionalRoles().add(UserRole.SCOUT);
        var dto = admin.changePrimaryRole("a2", "ATHLETE", "super-1");
        assertThat(dto.roles()).containsExactlyInAnyOrder("ATHLETE", "SCOUT");
        verify(tokens).revokeAllByUser(u);
    }

    @Test
    void cannotChangeOwnRole() {
        user("super-1", UserRole.SUPER_ADMIN);
        assertThatThrownBy(() -> admin.changePrimaryRole("super-1", "ATHLETE", "super-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createAdminIsActiveAndVerified() {
        when(users.existsByEmail("new@x.com")).thenReturn(false);
        var dto = admin.createAdmin("New@X.com", "Password123!", "super-1");
        assertThat(dto.role()).isEqualTo("ADMIN");
        assertThat(dto.status()).isEqualTo("ACTIVE");
        assertThat(dto.emailVerified()).isTrue();
        assertThat(dto.email()).isEqualTo("new@x.com");
    }
}
