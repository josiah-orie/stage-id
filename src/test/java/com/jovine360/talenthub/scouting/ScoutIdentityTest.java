package com.jovine360.talenthub.scouting;

import com.jovine360.talenthub.activityconfiguration.application.service.ActivityConfigurationService;
import com.jovine360.talenthub.activityconfiguration.domain.model.ActivityType;
import com.jovine360.talenthub.activityconfiguration.domain.model.ActivityTypeCode;
import com.jovine360.talenthub.activityconfiguration.domain.repository.*;
import com.jovine360.talenthub.scout.api.ScoutApi;
import com.jovine360.talenthub.scouting.application.service.ScoutEvaluationService;
import com.jovine360.talenthub.scouting.application.service.WatchlistService;
import com.jovine360.talenthub.scouting.domain.model.Watchlist;
import com.jovine360.talenthub.scouting.infrastructure.web.ScoutingController;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Scout data is keyed by the caller's token; activity types are persisted. */
class ScoutIdentityTest {

    private static AuthenticatedPrincipal scout(String id) {
        return AuthenticatedPrincipal.of(id, id + "@x.com", List.of("SCOUT"));
    }

    @Test
    void favouritesUseTheCallersOwnScoutProfile() {
        WatchlistService watchlists = mock(WatchlistService.class);
        ScoutApi scoutApi = mock(ScoutApi.class);
        when(scoutApi.requireScoutProfileIdForUser("user-a")).thenReturn("scout-profile-a");
        var controller = new ScoutingController(watchlists, mock(ScoutEvaluationService.class), scoutApi,
                mock(com.jovine360.talenthub.scouting.application.service.ScoutNoteService.class));

        controller.getFavourites(scout("user-a"));

        verify(watchlists).getFavourites("scout-profile-a");
        verify(watchlists, never()).getFavourites("scout-profile-b");
    }

    @Test
    void privateWatchlistOfAnotherScoutIsForbidden() {
        var repo = mock(com.jovine360.talenthub.scouting.domain.repository.WatchlistRepository.class);
        Watchlist w = new Watchlist();
        w.setId("wl-1");
        w.setOwnerId("user-b");
        w.setShared(false);
        when(repo.findById("wl-1")).thenReturn(Optional.of(w));
        WatchlistService service = new WatchlistService(
                mock(com.jovine360.talenthub.scouting.domain.repository.ScoutFavouriteRepository.class),
                repo,
                mock(com.jovine360.talenthub.scouting.domain.repository.WatchlistAthleteRepository.class),
                mock(com.jovine360.talenthub.scouting.domain.repository.AthleteFollowRepository.class),
                mock(com.jovine360.talenthub.shared.domain.service.EntitlementService.class),
                mock(ApplicationEventPublisher.class));

        assertThatThrownBy(() -> service.getReadableWatchlist("wl-1", "user-a")).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> service.assertOwner("wl-1", "user-a")).isInstanceOf(SecurityException.class);
        assertThat(service.getReadableWatchlist("wl-1", "user-b")).isSameAs(w);

        w.setShared(true);
        assertThat(service.getReadableWatchlist("wl-1", "user-a")).isSameAs(w);
    }

    @Test
    void activityTypeIsSavedAndDuplicateCodeIsConflict() {
        ActivityTypeRepository types = mock(ActivityTypeRepository.class);
        when(types.save(any(ActivityType.class))).thenAnswer(i -> {
            ActivityType t = i.getArgument(0);
            t.setId("type-1");
            return t;
        });
        var service = new ActivityConfigurationService(mock(ActivityTemplateRepository.class), types,
                mock(ActivitySchemaVersionRepository.class), mock(ActivityFieldDefinitionRepository.class),
                mock(ActivityFieldOptionRepository.class), mock(ApplicationEventPublisher.class));

        ActivityType created = service.createActivityType(ActivityTypeCode.SHOWCASE, "Showcase", null);
        assertThat(created.getId()).isEqualTo("type-1");
        verify(types).save(any(ActivityType.class));

        when(types.findByCode(ActivityTypeCode.SHOWCASE)).thenReturn(Optional.of(created));
        assertThatThrownBy(() -> service.createActivityType(ActivityTypeCode.SHOWCASE, "Again", null))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
