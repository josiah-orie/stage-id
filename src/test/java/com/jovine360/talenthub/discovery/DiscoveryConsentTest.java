package com.jovine360.talenthub.discovery;

import com.jovine360.talenthub.discovery.application.service.AthleteDiscoveryService;
import com.jovine360.talenthub.discovery.domain.model.AthleteSearchProjection;
import com.jovine360.talenthub.discovery.domain.repository.AthleteSearchProjectionRepository;
import com.jovine360.talenthub.discovery.domain.repository.SavedSearchRepository;
import com.jovine360.talenthub.safeguarding.api.AthleteConsentGrantedEvent;
import com.jovine360.talenthub.safeguarding.domain.event.AthleteConsentRevokedEvent;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Withdrawing discovery consent hides the athlete; granting shows them again. */
class DiscoveryConsentTest {

    @Test
    void consentWithdrawalHidesAndGrantRestores() throws Exception {
        AthleteSearchProjectionRepository repo = mock(AthleteSearchProjectionRepository.class);
        AthleteSearchProjection p = new AthleteSearchProjection();
        p.setAthleteId("a-1");
        when(repo.findByAthleteId("a-1")).thenReturn(Optional.of(p));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        AthleteDiscoveryService service = new AthleteDiscoveryService(repo, mock(SavedSearchRepository.class),
                mock(com.jovine360.talenthub.shared.domain.service.EntitlementService.class));

        assertThat(service.getDiscoveryProfile("a-1")).isSameAs(p);

        invoke(service, "onConsentRevoked", new AthleteConsentRevokedEvent("a-1", "PROFILE_DISCOVERY", "a-1"));
        assertThat(p.getVisibilityLevel()).isEqualTo("PRIVATE");
        assertThatThrownBy(() -> service.getDiscoveryProfile("a-1")).isInstanceOf(EntityNotFoundException.class);

        invoke(service, "onConsentRevoked", new AthleteConsentRevokedEvent("a-1", "MEDIA_SHARING", "a-1"));
        assertThat(p.getVisibilityLevel()).isEqualTo("PRIVATE");

        invoke(service, "onConsentGranted", new AthleteConsentGrantedEvent("a-1", "PROFILE_DISCOVERY", "a-1"));
        assertThat(p.getVisibilityLevel()).isEqualTo("PUBLIC");
    }

    private static void invoke(Object target, String name, Object arg) throws Exception {
        Method m = target.getClass().getDeclaredMethod(name, arg.getClass());
        m.setAccessible(true);
        m.invoke(target, arg);
    }
}
