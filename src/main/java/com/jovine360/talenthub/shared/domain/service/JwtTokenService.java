package com.jovine360.talenthub.shared.domain.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Issues and validates HMAC-SHA256 JWTs using jjwt 0.12.6.
 * Access tokens carry userId, email, and roles claims.
 */
@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_EMAIL   = "email";
    private static final String CLAIM_ROLES   = "roles";

    private final SecretKey signingKey;
    private final long accessTokenExpiryMinutes;
    private final long refreshTokenExpiryDays;

    public JwtTokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token.expiry-minutes:60}") long accessTokenExpiryMinutes,
            @Value("${jwt.refresh-token.expiry-days:30}") long refreshTokenExpiryDays) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMinutes = accessTokenExpiryMinutes;
        this.refreshTokenExpiryDays = refreshTokenExpiryDays;
    }

    /**
     * Generates a signed access JWT for the given user.
     */
    public String generateAccessToken(String userId, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpiryMinutes * 60);

        return Jwts.builder()
                .subject(userId)
                .claims(Map.of(
                        CLAIM_USER_ID, userId,
                        CLAIM_EMAIL, email,
                        CLAIM_ROLES, roles
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates the token signature and expiry.
     *
     * @return true if the token is valid
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the subject (userId) from a validated token.
     */
    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the email claim from a validated token.
     */
    public String extractEmail(String token) {
        return (String) parseClaims(token).get(CLAIM_EMAIL);
    }

    /**
     * Extracts the roles claim from a validated token.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get(CLAIM_ROLES);
        if (roles instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    /**
     * Returns the access token expiry duration in minutes.
     */
    public long getAccessTokenExpiryMinutes() {
        return accessTokenExpiryMinutes;
    }

    /**
     * Returns the refresh token expiry duration in days.
     */
    public long getRefreshTokenExpiryDays() {
        return refreshTokenExpiryDays;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
