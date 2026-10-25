package com.jovine360.talenthub.shared.infrastructure.configuration;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Immutable record representing the authenticated user principal stored in the
 * Spring Security context after JWT validation.
 */
public record AuthenticatedPrincipal(
        String userId,
        String email,
        List<String> roles
) implements UserDetails {

    public static AuthenticatedPrincipal of(String userId, String email, List<String> roles) {
        return new AuthenticatedPrincipal(userId, email, roles);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    @Override
    public String getPassword() {
        return null; // Password is not stored in the JWT principal
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
