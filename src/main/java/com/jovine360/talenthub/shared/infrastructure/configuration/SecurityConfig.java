package com.jovine360.talenthub.shared.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

/**
 * Stateless JWT-based security configuration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiErrorSecurityHandlers apiErrorSecurityHandlers;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          ApiErrorSecurityHandlers apiErrorSecurityHandlers) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.apiErrorSecurityHandlers = apiErrorSecurityHandlers;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .addHeaderWriter(new StaticHeadersWriter(
                        "Strict-Transport-Security", "max-age=31536000; includeSubDomains"))
                .addHeaderWriter(new StaticHeadersWriter(
                        "X-Content-Type-Options", "nosniff"))
                .addHeaderWriter(new StaticHeadersWriter(
                        "X-Frame-Options", "DENY"))
                .addHeaderWriter(new StaticHeadersWriter(
                        "Content-Security-Policy", "default-src 'self'"))
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(apiErrorSecurityHandlers)
                .accessDeniedHandler(apiErrorSecurityHandlers)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    // Public auth endpoints. /auth/logout and /auth/mfa/verify are NOT listed:
                    // they require an authenticated caller.
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/verify-email",
                    "/api/v1/auth/password-reset",
                    "/api/v1/auth/password-reset/confirm",
                    "/api/v1/payments/webhooks/**",
                    "/actuator/health",
                    "/actuator/info",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
