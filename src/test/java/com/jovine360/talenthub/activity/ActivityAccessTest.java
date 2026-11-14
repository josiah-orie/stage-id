package com.jovine360.talenthub.activity;

import com.jovine360.talenthub.activity.application.service.ActivityQueryService;
import com.jovine360.talenthub.activity.application.service.ActivitySubmissionService;
import com.jovine360.talenthub.activity.domain.model.ActivityStatus;
import com.jovine360.talenthub.activity.domain.model.AthleteActivity;
import com.jovine360.talenthub.activity.domain.model.VisibilityLevel;
import com.jovine360.talenthub.activity.infrastructure.web.AthleteActivityController;
import com.jovine360.talenthub.athleteaccess.api.AthleteAccessPolicy;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.exception.SafeguardingViolationException;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Read access rules on athlete activities. No Spring context. */
class ActivityAccessTest {

    private static final String ATHLETE = "athlete-1";

    private ActivityQueryService query;
    private AthleteAccessPolicy policy;
    private AthleteActivityController controller;

    @BeforeEach
    void setUp() {
        query = mock(ActivityQueryService.class);
        policy = mock(AthleteAccessPolicy.class);
        when(policy.canAccess(eq(ATHLETE), eq(ATHLETE), any(), any())).thenReturn(true);
        doThrow(new SafeguardingViolationException("ACCESS_DENIED", "denied"))
                .when(policy).assertCanAccess(eq(ATHLETE), eq("stranger"), any(), any());
        when(query.getAthleteActivities(anyString(), any(), org.mockito.ArgumentMatchers.anyBoolean()))
                .thenReturn(PageResponse.empty(0, 20));
        controller = new AthleteActivityController(mock(ActivitySubmissionService.class), query, policy);
    }

    private static AuthenticatedPrincipal user(String id, String role) {
        return AuthenticatedPrincipal.of(id, id + "@x.com", List.of(role));
    }

    private AthleteActivity activity(ActivityStatus status, VisibilityLevel visibility) {
        AthleteActivity a = new AthleteActivity();
        a.setId("act-1");
        a.setAthleteId(ATHLETE);
        a.setActivityStatus(status);
        a.setVisibilityLevel(visibility);
        when(query.getById("act-1")).thenReturn(a);
        return a;
    }

    @Test
    void ownerSeesAllActivities() {
        assertThat(controller.listActivities(ATHLETE, Pageable.unpaged(), user(ATHLETE, "ATHLETE"))
                .getStatusCode().value()).isEqualTo(200);
        verify(query).getAthleteActivities(ATHLETE, Pageable.unpaged(), false);
    }

    @Test
    void scoutSeesOnlyPublicPublished() {
        controller.listActivities(ATHLETE, Pageable.unpaged(), user("scout-1", "SCOUT"));
        verify(query).getAthleteActivities(ATHLETE, Pageable.unpaged(), true);

        activity(ActivityStatus.DRAFT, VisibilityLevel.PUBLIC);
        assertThatThrownBy(() -> controller.getActivity("act-1", user("scout-1", "SCOUT")))
                .isInstanceOf(EntityNotFoundException.class);

        activity(ActivityStatus.PUBLISHED, VisibilityLevel.PUBLIC);
        assertThat(controller.getActivity("act-1", user("scout-1", "SCOUT")).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void unrelatedUserIsForbidden() {
        assertThatThrownBy(() -> controller.listActivities(ATHLETE, Pageable.unpaged(), user("stranger", "COACH")))
                .isInstanceOf(SafeguardingViolationException.class);

        activity(ActivityStatus.PUBLISHED, VisibilityLevel.PUBLIC);
        assertThatThrownBy(() -> controller.getActivity("act-1", user("stranger", "COACH")))
                .isInstanceOf(SafeguardingViolationException.class);
    }
}
