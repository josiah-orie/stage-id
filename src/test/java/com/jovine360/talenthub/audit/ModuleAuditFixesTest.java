package com.jovine360.talenthub.audit;

import com.jovine360.talenthub.athleteaccess.api.AthleteAccessPolicy;
import com.jovine360.talenthub.notification.application.service.NotificationService;
import com.jovine360.talenthub.notification.domain.model.Notification;
import com.jovine360.talenthub.notification.domain.repository.EmailOutboxRepository;
import com.jovine360.talenthub.notification.domain.repository.NotificationPreferenceRepository;
import com.jovine360.talenthub.notification.domain.repository.NotificationRepository;
import com.jovine360.talenthub.organization.api.OrganizationApi;
import com.jovine360.talenthub.profile.api.ProfileController;
import com.jovine360.talenthub.profile.api.UserProfileDto;
import com.jovine360.talenthub.profile.application.service.ProfileService;
import com.jovine360.talenthub.safeguarding.application.service.DataSubjectRequestService;
import com.jovine360.talenthub.safeguarding.domain.model.DataSubjectRequest;
import com.jovine360.talenthub.safeguarding.domain.repository.DataSubjectRequestRepository;
import com.jovine360.talenthub.scout.api.ScoutApi;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import com.jovine360.talenthub.shared.domain.exception.SafeguardingViolationException;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import com.jovine360.talenthub.subscription.api.SubscriptionController;
import com.jovine360.talenthub.subscription.application.service.SubscriptionLifecycleService;
import com.jovine360.talenthub.subscription.domain.model.UserSubscription;
import com.jovine360.talenthub.subscription.domain.repository.SubscriptionAddonRepository;
import com.jovine360.talenthub.subscription.domain.repository.SubscriptionPlanRepository;
import com.jovine360.talenthub.subscription.domain.repository.UserSubscriptionRepository;
import com.jovine360.talenthub.verification.application.service.VerificationService;
import com.jovine360.talenthub.verification.domain.repository.ActivityVerificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Ownership and privacy fixes found during the module audit. No Spring context. */
class ModuleAuditFixesTest {

    private static AuthenticatedPrincipal user(String id, String role) {
        return AuthenticatedPrincipal.of(id, id + "@x.com", List.of(role));
    }

    @Test
    void coachNeedsAccessToTheAthleteToVerify() {
        com.jovine360.talenthub.activity.api.ActivityApi activities = mock(com.jovine360.talenthub.activity.api.ActivityApi.class);
        when(activities.findAthleteId("act-1")).thenReturn(Optional.of("athlete-1"));
        AthleteAccessPolicy policy = mock(AthleteAccessPolicy.class);
        doThrow(new SafeguardingViolationException("ACCESS_DENIED", "no"))
                .when(policy).assertCanAccess(eq("athlete-1"), eq("coach-x"), any(), any());
        VerificationService service = new VerificationService(activities, mock(ActivityVerificationRepository.class),
                mock(ApplicationEventPublisher.class), policy);

        assertThatThrownBy(() -> service.verifyActivity("act-1", "coach-x", List.of("COACH")))
                .isInstanceOf(SafeguardingViolationException.class);
        assertThatThrownBy(() -> service.verifyActivity("act-1", "athlete-1", List.of("COACH")))
                .isInstanceOf(SecurityException.class);
        verify(activities, never()).markVerified(any());
        service.verifyActivity("act-1", "coach-ok", List.of("COACH"));
        verify(activities).markVerified("act-1");
    }

    @Test
    void dataSubjectRequestLifecycle() {
        DataSubjectRequestRepository repo = mock(DataSubjectRequestRepository.class);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        DataSubjectRequestService service = new DataSubjectRequestService(repo);

        DataSubjectRequest r = service.submit("u1", "deletion");
        assertThat(r.getRequestType()).isEqualTo("DELETION");
        assertThat(r.getStatus()).isEqualTo("PENDING");
        assertThatThrownBy(() -> service.submit("u1", "PORTABILITY")).isInstanceOf(IllegalArgumentException.class);

        when(repo.findByRequesterIdOrderBySubmittedAtDesc("u1")).thenReturn(List.of(r));
        assertThatThrownBy(() -> service.submit("u1", "DELETION")).isInstanceOf(DuplicateResourceException.class);

        r.setId("dsr-1");
        when(repo.findById("dsr-1")).thenReturn(Optional.of(r));
        assertThat(service.resolve("dsr-1", "completed").getStatus()).isEqualTo("COMPLETED");
        assertThatThrownBy(() -> service.resolve("dsr-1", "REJECTED")).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void subscriptionAddonsAndCancelAreLimitedToTheOwner() {
        UserSubscriptionRepository subs = mock(UserSubscriptionRepository.class);
        UserSubscription sub = new UserSubscription();
        sub.setId("sub-1");
        sub.setSubscriberId("org-1");
        when(subs.findById("sub-1")).thenReturn(Optional.of(sub));
        OrganizationApi orgs = mock(OrganizationApi.class);
        when(orgs.isOrganizationAdmin("org-1", "owner-1")).thenReturn(true);
        ScoutApi scouts = mock(ScoutApi.class);
        when(scouts.findScoutProfileIdByUserId(anyString())).thenReturn(Optional.empty());
        SubscriptionAddonRepository addons = mock(SubscriptionAddonRepository.class);
        var access = new com.jovine360.talenthub.subscription.application.service.SubscriptionAccessService(
                subs, mock(SubscriptionLifecycleService.class), orgs, scouts);
        var controller = new SubscriptionController(mock(SubscriptionLifecycleService.class), subs,
                mock(SubscriptionPlanRepository.class), addons, access);

        assertThatThrownBy(() -> controller.getAddons("sub-1", user("stranger", "ATHLETE")))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> controller.cancelSubscription("sub-1", false, null, user("stranger", "ATHLETE")))
                .isInstanceOf(SecurityException.class);
        assertThat(controller.getAddons("sub-1", user("owner-1", "ORG_ADMIN")).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.getAddons("sub-1", user("admin-1", "ADMIN")).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void otherUsersSeeOnlyPublicProfileFields() {
        ProfileService profiles = mock(ProfileService.class);
        when(profiles.getProfileByUserId("u1")).thenReturn(new UserProfileDto("p1", "u1", "Ada", "Obi", "Ada O.",
                LocalDate.of(2009, 1, 1), "F", "+2348000000", "NG", "Lagos", "Winger", null, "ATHLETE", true));
        var controller = new ProfileController(profiles);

        UserProfileDto other = controller.getProfile("u1", user("scout-1", "SCOUT")).getBody();
        assertThat(other.displayName()).isEqualTo("Ada O.");
        assertThat(other.firstName()).isNull();
        assertThat(other.dateOfBirth()).isNull();
        assertThat(other.phoneNumber()).isNull();

        assertThat(controller.getProfile("u1", user("u1", "ATHLETE")).getBody().phoneNumber()).isEqualTo("+2348000000");
        assertThat(controller.getProfile("u1", user("admin", "ADMIN")).getBody().dateOfBirth()).isNotNull();
    }

    @Test
    void cannotMarkSomeoneElsesNotificationRead() {
        NotificationRepository repo = mock(NotificationRepository.class);
        Notification n = new Notification();
        n.setId("n1");
        n.setRecipientId("u1");
        when(repo.findById("n1")).thenReturn(Optional.of(n));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        NotificationService service = new NotificationService(repo, mock(NotificationPreferenceRepository.class),
                mock(EmailOutboxRepository.class));

        assertThatThrownBy(() -> service.markRead("n1", "u2")).isInstanceOf(EntityNotFoundException.class);
        assertThat(service.markRead("n1", "u1").isRead()).isTrue();
    }
}
