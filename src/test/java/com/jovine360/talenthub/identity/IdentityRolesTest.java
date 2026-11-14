package com.jovine360.talenthub.identity;

import com.jovine360.talenthub.identity.application.command.RegisterUserCommand;
import com.jovine360.talenthub.identity.application.service.AuthService;
import com.jovine360.talenthub.identity.application.service.UserAccountService;
import com.jovine360.talenthub.identity.domain.model.User;
import com.jovine360.talenthub.identity.domain.model.UserRole;
import com.jovine360.talenthub.identity.domain.repository.RefreshTokenRepository;
import com.jovine360.talenthub.identity.domain.repository.UserRepository;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Registration lockdown and multi-role account behaviour. No Spring context. */
class IdentityRolesTest {

    private UserRepository userRepository;
    private AuthService authService;
    private UserAccountService accountService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User saved = i.getArgument(0);
            if (saved.getId() == null) saved.setId("generated-id");
            return saved;
        });
        authService = new AuthService(userRepository, mock(RefreshTokenRepository.class), encoder,
                mock(JwtTokenService.class), mock(ApplicationEventPublisher.class));
        accountService = new UserAccountService(userRepository);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"ADMIN", "SUPER_ADMIN", "ORG_ADMIN"})
    void registrationRejectsPrivilegedRoles(UserRole role) {
        assertThatThrownBy(() -> authService.register(
                new RegisterUserCommand("a@b.com", "Password123!", role)))
                .isInstanceOf(IllegalArgumentException.class);
        Mockito.verify(userRepository, Mockito.never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"ATHLETE", "COACH", "PARENT", "GUARDIAN", "AGENT", "SCOUT"})
    void registrationAllowsOrdinaryRoles(UserRole role) {
        assertThat(authService.register(new RegisterUserCommand("a@b.com", "Password123!", role)))
                .isNotNull();
    }

    @Test
    void assigningExtraRoleAddsToAllRoles() {
        User u = user(UserRole.ATHLETE);
        when(userRepository.findById("u1")).thenReturn(Optional.of(u));

        var dto = accountService.assignRole("u1", UserRole.AGENT);

        assertThat(dto.role()).isEqualTo("ATHLETE");
        assertThat(dto.roles()).containsExactly("ATHLETE", "AGENT");
    }

    @Test
    void assigningPrivilegedRoleIsRejected() {
        assertThatThrownBy(() -> accountService.assignRole("u1", UserRole.ADMIN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void assigningExistingRoleIsConflict() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(user(UserRole.COACH)));
        assertThatThrownBy(() -> accountService.assignRole("u1", UserRole.COACH))
                .isInstanceOf(DuplicateResourceException.class);
    }

    private static User user(UserRole role) {
        User u = new User();
        u.setId("u1");
        u.setEmail("a@b.com");
        u.setRole(role);
        u.setStatus(com.jovine360.talenthub.identity.domain.model.UserStatus.ACTIVE);
        return u;
    }
}
