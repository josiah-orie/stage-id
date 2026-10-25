package com.jovine360.talenthub.shared;

import com.jovine360.talenthub.shared.domain.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtTokenService}.
 * No Spring context — pure unit test.
 */
class JwtTokenServiceTest {

    private static final String SECRET =
            "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256-algorithm";

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(SECRET, 60L, 30L);
    }

    @Test
    void generatesValidToken() {
        String token = jwtTokenService.generateAccessToken("user-id-1", "alice@example.com",
                List.of("ATHLETE"));
        assertThat(token).isNotBlank();
        assertThat(jwtTokenService.isTokenValid(token)).isTrue();
    }

    @Test
    void extractsUserIdFromToken() {
        String token = jwtTokenService.generateAccessToken("user-123", "alice@example.com",
                List.of("ATHLETE"));
        assertThat(jwtTokenService.extractUserId(token)).isEqualTo("user-123");
    }

    @Test
    void extractsEmailFromToken() {
        String token = jwtTokenService.generateAccessToken("u1", "bob@example.com", List.of("SCOUT"));
        assertThat(jwtTokenService.extractEmail(token)).isEqualTo("bob@example.com");
    }

    @Test
    void extractsRolesFromToken() {
        List<String> roles = List.of("ATHLETE", "GUARDIAN");
        String token = jwtTokenService.generateAccessToken("u1", "carol@example.com", roles);
        assertThat(jwtTokenService.extractRoles(token)).containsExactlyInAnyOrderElementsOf(roles);
    }

    @Test
    void invalidTokenReturnsFalse() {
        assertThat(jwtTokenService.isTokenValid("not.a.token")).isFalse();
    }

    @Test
    void tamperedTokenReturnsFalse() {
        String token = jwtTokenService.generateAccessToken("u1", "user@example.com", List.of("ATHLETE"));
        String tampered = token + "x";
        assertThat(jwtTokenService.isTokenValid(tampered)).isFalse();
    }

    @Test
    void nullTokenReturnsFalse() {
        assertThat(jwtTokenService.isTokenValid(null)).isFalse();
    }
}
