package com.jovine360.talenthub.shared;

import com.jovine360.talenthub.shared.api.GlobalExceptionHandler;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.exception.EntitlementLimitExceededException;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import com.jovine360.talenthub.shared.domain.exception.SafeguardingViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 * No Spring context — instantiates the handler directly.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("GET", "/api/v1/test");
    }

    @Test
    void entityNotFoundReturns404() {
        var response = handler.handleNotFound(
                new EntityNotFoundException("Athlete", "abc-123"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    void duplicateResourceReturns409() {
        var response = handler.handleDuplicate(
                new DuplicateResourceException("User", "email", "alice@example.com"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void invalidTransitionReturns409() {
        var response = handler.handleInvalidTransition(
                new InvalidStateTransitionException("Athlete", "PENDING", "ACTIVE"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void entitlementLimitReturns402() {
        var response = handler.handleEntitlementLimit(
                new EntitlementLimitExceededException("max_athletes", 5, 6), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
    }

    @Test
    void safeguardingViolationReturns403() {
        var response = handler.handleSafeguarding(
                new SafeguardingViolationException("MINOR_CONSENT", "Guardian consent required"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void genericExceptionReturns500() {
        ResponseEntity<?> response = handler.handleGeneric(
                new RuntimeException("unexpected"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
