package com.jovine360.talenthub.guardian;

import com.jovine360.talenthub.identity.api.UserDirectoryApi;
import com.jovine360.talenthub.guardian.api.CreateGuardianRelationshipRequest;
import com.jovine360.talenthub.guardian.api.GuardianLinkActivatedEvent;
import com.jovine360.talenthub.guardian.api.GuardianLinkEndedEvent;
import com.jovine360.talenthub.guardian.application.service.GuardianService;
import com.jovine360.talenthub.guardian.domain.model.GuardianLink;
import com.jovine360.talenthub.guardian.domain.model.GuardianLinkStatus;
import com.jovine360.talenthub.guardian.domain.model.GuardianRelationshipType;
import com.jovine360.talenthub.guardian.domain.repository.GuardianLinkRepository;
import com.jovine360.talenthub.guardian.domain.service.SafeguardingPolicyService;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Guardian relationship lifecycle. No Spring context, in-memory repository stub. */
class GuardianServiceTest {

    private static final String ATHLETE = "athlete-1";
    private static final String PARENT = "parent-1";

    private final List<GuardianLink> store = new ArrayList<>();
    private GuardianLinkRepository repo;
    private SafeguardingPolicyService safeguarding;
    private ApplicationEventPublisher events;
    private GuardianService service;

    @BeforeEach
    void setUp() {
        repo = mock(GuardianLinkRepository.class);
        safeguarding = mock(SafeguardingPolicyService.class);
        events = mock(ApplicationEventPublisher.class);
        UserDirectoryApi directory = mock(UserDirectoryApi.class);
        when(directory.userHasRole(ATHLETE, "ATHLETE")).thenReturn(true);
        when(repo.save(any(GuardianLink.class))).thenAnswer(i -> {
            GuardianLink l = i.getArgument(0);
            if (l.getId() == null) l.setId("link-" + (store.size() + 1));
            if (!store.contains(l)) store.add(l);
            return l;
        });
        when(repo.findById(any())).thenAnswer(i -> store.stream().filter(l -> l.getId().equals(i.getArgument(0))).findFirst());
        when(repo.findByGuardianUserIdAndAthleteUserId(any(), any())).thenAnswer(i -> store.stream()
                .filter(l -> l.getGuardianUserId().equals(i.getArgument(0)) && l.getAthleteUserId().equals(i.getArgument(1)))
                .toList());
        when(repo.findByGuardianUserIdAndStatus(any(), any())).thenAnswer(i -> store.stream()
                .filter(l -> l.getGuardianUserId().equals(i.getArgument(0)) && l.getStatus() == i.getArgument(1))
                .toList());
        when(repo.findByGuardianUserIdOrAthleteUserIdOrderByCreatedAtDesc(any(), any())).thenAnswer(i -> store.stream()
                .filter(l -> l.getGuardianUserId().equals(i.getArgument(0)) || l.getAthleteUserId().equals(i.getArgument(1)))
                .toList());
        service = new GuardianService(repo, safeguarding, directory, events);
    }

    private CreateGuardianRelationshipRequest req(GuardianRelationshipType t, String doc, String perm) {
        return new CreateGuardianRelationshipRequest(ATHLETE, t, null, doc, perm);
    }

    @Test
    void parentLifecycle_requestApproveList() {
        var created = service.requestLink(PARENT, List.of("PARENT"), req(GuardianRelationshipType.PARENT, null, null));
        assertThat(created.status()).isEqualTo("PENDING");

        var approved = service.approve(created.id(), ATHLETE, List.of("ATHLETE"));
        assertThat(approved.status()).isEqualTo("ACTIVE");
        assertThat(approved.consentGiven()).isTrue();

        assertThat(service.hasActiveGuardianLink(PARENT, ATHLETE)).isTrue();
        assertThat(service.listGuardedAthletes(PARENT)).hasSize(1);
        verify(events).publishEvent(any(GuardianLinkActivatedEvent.class));
    }

    @Test
    void duplicateOpenRequestIsConflict() {
        service.requestLink(PARENT, List.of("PARENT"), req(GuardianRelationshipType.PARENT, null, null));
        assertThatThrownBy(() -> service.requestLink(PARENT, List.of("PARENT"), req(GuardianRelationshipType.PARENT, null, null)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void nonOwnerCannotApprove() {
        var created = service.requestLink(PARENT, List.of("PARENT"), req(GuardianRelationshipType.PARENT, null, null));
        assertThatThrownBy(() -> service.approve(created.id(), "someone-else", List.of("ATHLETE")))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> service.approve(created.id(), PARENT, List.of("PARENT")))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void wrongRoleCannotRequest() {
        assertThatThrownBy(() -> service.requestLink(PARENT, List.of("ATHLETE"), req(GuardianRelationshipType.AGENT, null, null)))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void legalGuardianNeedsDocumentAndAdminApprovalAndExpires() {
        assertThatThrownBy(() -> service.requestLink(PARENT, List.of("GUARDIAN"), req(GuardianRelationshipType.LEGAL_GUARDIAN, null, null)))
                .isInstanceOf(IllegalArgumentException.class);

        var created = service.requestLink(PARENT, List.of("GUARDIAN"), req(GuardianRelationshipType.LEGAL_GUARDIAN, "doc-1", null));
        assertThat(created.status()).isEqualTo("PENDING_ADMIN_VERIFICATION");

        // the athlete cannot approve a legal guardian link, an admin must
        assertThatThrownBy(() -> service.approve(created.id(), ATHLETE, List.of("ATHLETE")))
                .isInstanceOf(SecurityException.class);

        var approved = service.approve(created.id(), "admin-1", List.of("ADMIN"));
        assertThat(approved.status()).isEqualTo("ACTIVE");
        assertThat(approved.expiresAt()).isAfter(Instant.now().plusSeconds(300L * 24 * 3600));
    }

    @Test
    void expiredLinkIsNotActiveAndShowsExpired() {
        var created = service.requestLink(PARENT, List.of("GUARDIAN"), req(GuardianRelationshipType.LEGAL_GUARDIAN, "doc-1", null));
        service.approve(created.id(), "admin-1", List.of("ADMIN"));
        store.get(0).setExpiresAt(Instant.now().minusSeconds(5));

        assertThat(service.hasActiveGuardianLink(PARENT, ATHLETE)).isFalse();
        assertThat(service.listMine(PARENT).get(0).status()).isEqualTo("EXPIRED");
    }

    @Test
    void agentApprovalPublishesGrantEventAndRevokeEndsIt() {
        var created = service.requestLink("agent-1", List.of("AGENT"), req(GuardianRelationshipType.AGENT, null, "manage"));
        assertThat(created.permission()).isEqualTo("MANAGE");

        service.approve(created.id(), ATHLETE, List.of("ATHLETE"));
        // agents never get guardian-level access directly
        assertThat(service.hasActiveGuardianLink("agent-1", ATHLETE)).isFalse();

        service.revoke(created.id(), "agent-1", List.of("AGENT"));
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(events, org.mockito.Mockito.times(2)).publishEvent(captor.capture());
        assertThat(captor.getAllValues().get(0)).isInstanceOf(GuardianLinkActivatedEvent.class);
        assertThat(captor.getAllValues().get(1)).isInstanceOf(GuardianLinkEndedEvent.class);
    }

    @Test
    void rejectedLinkCannotBeApproved() {
        var created = service.requestLink(PARENT, List.of("PARENT"), req(GuardianRelationshipType.PARENT, null, null));
        service.reject(created.id(), ATHLETE, List.of("ATHLETE"), "not my parent");
        assertThatThrownBy(() -> service.approve(created.id(), ATHLETE, List.of("ATHLETE")))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void unrelatedUserCannotRevoke() {
        var created = service.requestLink(PARENT, List.of("PARENT"), req(GuardianRelationshipType.PARENT, null, null));
        assertThatThrownBy(() -> service.revoke(created.id(), "stranger", List.of("SCOUT")))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void minorRuleDelegatesToSafeguardingPolicy() {
        service.assertGuardianConsentForMinor(ATHLETE);
        verify(safeguarding).assertGuardianConsent(ATHLETE);
        verifyNoInteractions(events);
    }
}
