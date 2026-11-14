package com.jovine360.talenthub.shared;

import com.jovine360.talenthub.shared.api.GlobalExceptionHandler;
import com.jovine360.talenthub.shared.infrastructure.configuration.ApiErrorSecurityHandlers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for the JSON 401/403 envelope written by the security filter chain. */
class ApiErrorSecurityHandlersTest {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private ApiErrorSecurityHandlers handlers;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handlers = new ApiErrorSecurityHandlers(mapper);
        request = new MockHttpServletRequest("GET", "/api/v1/athletes/me");
        response = new MockHttpServletResponse();
    }

    @Test
    void missingTokenReturns401Envelope() throws Exception {
        handlers.commence(request, response, new BadCredentialsException("x"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        JsonNode body = mapper.readTree(response.getContentAsString());
        assertThat(body.get("status").asInt()).isEqualTo(401);
        assertThat(body.get("error").asString()).isEqualTo("Unauthorized");
        assertThat(body.get("path").asString()).isEqualTo("/api/v1/athletes/me");
        assertThat(body.has("timestamp")).isTrue();
    }

    @Test
    void accessDeniedReturns403Envelope() throws Exception {
        handlers.handle(request, response, new AccessDeniedException("nope"));

        assertThat(response.getStatus()).isEqualTo(403);
        JsonNode body = mapper.readTree(response.getContentAsString());
        assertThat(body.get("status").asInt()).isEqualTo(403);
        assertThat(body.get("error").asString()).isEqualTo("Forbidden");
        assertThat(body.get("message").asString()).isEqualTo("Access denied");
    }

    @Test
    void securityExceptionFromServiceMapsTo403() {
        var handler = new GlobalExceptionHandler();
        var res = handler.handleSecurity(new SecurityException("Not authorised"),
                new MockHttpServletRequest("POST", "/api/v1/guardian-relationships/1/approve"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(res.getBody().message()).isEqualTo("Not authorised");
    }
}
