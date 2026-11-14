package com.jovine360.talenthub.shared.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS configuration for browser clients.
 *
 * <p>Picked up by Spring Security through {@code http.cors(...)} in {@link SecurityConfig},
 * so preflight {@code OPTIONS} requests are answered before authentication.
 * Origins, methods and headers are configurable via {@code cors.*} properties.
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:5173}")
    private List<String> allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private List<String> allowedMethods;

    @Value("${cors.allowed-headers:Authorization,Content-Type,Accept,Origin,X-Requested-With}")
    private List<String> allowedHeaders;

    @Value("${cors.exposed-headers:Location,Content-Disposition}")
    private List<String> exposedHeaders;

    @Value("${cors.allow-credentials:false}")
    private boolean allowCredentials;

    @Value("${cors.max-age-seconds:3600}")
    private long maxAgeSeconds;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(allowedMethods);
        config.setAllowedHeaders(allowedHeaders);
        config.setExposedHeaders(exposedHeaders);
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAgeSeconds);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
