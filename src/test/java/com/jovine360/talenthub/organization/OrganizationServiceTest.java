package com.jovine360.talenthub.organization;

import com.jovine360.talenthub.identity.api.UserDirectoryApi;
import com.jovine360.talenthub.identity.api.UserRoleGrantApi;
import com.jovine360.talenthub.organization.api.*;
import com.jovine360.talenthub.organization.application.service.OrganizationService;
import com.jovine360.talenthub.organization.domain.model.*;
import com.jovine360.talenthub.organization.domain.repository.*;
import com.jovine360.talenthub.organization.domain.service.OrgCodeGeneratorService;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Organization lifecycle with in-memory repository stubs. No Spring context. */
class OrganizationServiceTest {

    private static final String OWNER = "owner-1";
    private static final List<String> NO_ADMIN = List.of("ORG_ADMIN");

    private final List<Organization> orgs = new ArrayList<>();
    private final List<OrganizationMember> members = new ArrayList<>();
    private final List<OrganizationInvitation> invitations = new ArrayList<>();
    private final List<OrganizationAthleteRelationship> affiliations = new ArrayList<>();
    private UserRoleGrantApi roleGrant;
    private ApplicationEventPublisher events;
    private OrganizationService service;

    @SuppressWarnings("unchecked")
    private static <T extends BaseEntity> T save(List<T> store, T e) {
        if (e.getId() == null) e.setId(e.getClass().getSimpleName() + "-" + (store.size() + 1));
        if (!store.contains(e)) store.add(e);
        return e;
    }

    @BeforeEach
    void setUp() {
        OrganizationRepository orgRepo = mock(OrganizationRepository.class);
        when(orgRepo.save(any())).thenAnswer(i -> save(orgs, i.getArgument(0)));
        when(orgRepo.findById(anyString())).thenAnswer(i -> orgs.stream().filter(o -> o.getId().equals(i.getArgument(0))).findFirst());

        OrganizationMemberRepository memberRepo = mock(OrganizationMemberRepository.class);
        when(memberRepo.save(any())).thenAnswer(i -> save(members, i.getArgument(0)));
        when(memberRepo.findByOrganizationIdAndUserIdAndStatus(anyString(), anyString(), anyString())).thenAnswer(i ->
                members.stream().filter(m -> m.getOrganizationId().equals(i.getArgument(0))
                        && m.getUserId().equals(i.getArgument(1)) && m.getStatus().equals(i.getArgument(2))).findFirst());
        when(memberRepo.findByOrganizationIdAndStatus(anyString(), anyString())).thenAnswer(i ->
                members.stream().filter(m -> m.getOrganizationId().equals(i.getArgument(0)) && m.getStatus().equals(i.getArgument(1))).toList());
        when(memberRepo.findByUserIdAndStatus(anyString(), anyString())).thenAnswer(i ->
                members.stream().filter(m -> m.getUserId().equals(i.getArgument(0)) && m.getStatus().equals(i.getArgument(1))).toList());

        OrganizationInvitationRepository invRepo = mock(OrganizationInvitationRepository.class);
        when(invRepo.save(any())).thenAnswer(i -> save(invitations, i.getArgument(0)));
        when(invRepo.findByToken(anyString())).thenAnswer(i -> invitations.stream().filter(v -> v.getToken().equals(i.getArgument(0))).findFirst());
        when(invRepo.findByOrganizationIdAndStatus(anyString(), anyString())).thenAnswer(i ->
                invitations.stream().filter(v -> v.getOrganizationId().equals(i.getArgument(0)) && v.getStatus().equals(i.getArgument(1))).toList());

        OrganizationAthleteRelationshipRepository affRepo = mock(OrganizationAthleteRelationshipRepository.class);
        when(affRepo.save(any())).thenAnswer(i -> save(affiliations, i.getArgument(0)));
        when(affRepo.findById(anyString())).thenAnswer(i -> affiliations.stream().filter(a -> a.getId().equals(i.getArgument(0))).findFirst());
        when(affRepo.findByOrganizationIdAndAthleteUserIdAndStatusIn(anyString(), anyString(), any())).thenReturn(List.of());

        OrgCodeGeneratorService codes = mock(OrgCodeGeneratorService.class);
        when(codes.generate(anyString())).thenReturn("FCL");
        UserDirectoryApi directory = mock(UserDirectoryApi.class);
        when(directory.userHasRole("athlete-1", "ATHLETE")).thenReturn(true);
        roleGrant = mock(UserRoleGrantApi.class);
        events = mock(ApplicationEventPublisher.class);
        service = new OrganizationService(orgRepo, memberRepo, invRepo, affRepo, codes, roleGrant, directory, events);
    }

    private OrganizationDto createOrg() {
        return service.create(OWNER, new CreateOrganizationRequest("FC Lagos Academy", OrgType.ACADEMY, "NG", "Lagos", null));
    }

    @Test
    void creatorBecomesOwnerAndOrgAdminAndOrgStartsPending() {
        OrganizationDto org = createOrg();
        assertThat(org.status()).isEqualTo("PENDING_VERIFICATION");
        assertThat(service.isOrganizationAdmin(org.id(), OWNER)).isTrue();
        verify(roleGrant).grantRole(OWNER, "ORG_ADMIN");
    }

    @Test
    void invitationsAreBlockedUntilVerified() {
        OrganizationDto org = createOrg();
        assertThatThrownBy(() -> service.invite(org.id(), OWNER, NO_ADMIN,
                new InviteMemberRequest("coach@x.com", OrgMemberRole.COACH)))
                .isInstanceOf(SecurityException.class).hasMessageContaining("pending verification");
    }

    @Test
    void inviteAcceptListMembers() {
        OrganizationDto org = createOrg();
        service.verify(org.id(), "admin-1");

        InvitationDto inv = service.invite(org.id(), OWNER, NO_ADMIN, new InviteMemberRequest("Coach@X.com", OrgMemberRole.COACH));
        assertThat(inv.invitationToken()).isNotBlank();

        assertThatThrownBy(() -> service.acceptInvitation(org.id(), "someone", "other@x.com", inv.invitationToken()))
                .isInstanceOf(SecurityException.class);

        OrganizationMemberDto member = service.acceptInvitation(org.id(), "coach-1", "coach@x.com", inv.invitationToken());
        assertThat(member.memberRole()).isEqualTo("COACH");
        assertThat(service.listMembers(org.id(), "coach-1", List.of("COACH"))).hasSize(2);
        assertThat(service.organizationsWithAthleteAccess("coach-1")).containsExactly(org.id());

        assertThatThrownBy(() -> service.acceptInvitation(org.id(), "coach-1", "coach@x.com", inv.invitationToken()))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void nonMemberCannotReadMembersOrManage() {
        OrganizationDto org = createOrg();
        assertThatThrownBy(() -> service.listMembers(org.id(), "stranger", List.of("COACH")))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> service.update(org.id(), "stranger", List.of("ORG_ADMIN"),
                new UpdateOrganizationRequest("New name", null, null, null, null)))
                .isInstanceOf(SecurityException.class);
        // platform admins may read
        assertThat(service.listMembers(org.id(), "admin-1", List.of("ADMIN"))).hasSize(1);
    }

    @Test
    void athleteAffiliationApprovedByAthletePublishesEvent() {
        OrganizationDto org = createOrg();
        service.verify(org.id(), "admin-1");
        AthleteAffiliationDto req = service.requestAffiliation(org.id(), OWNER, NO_ADMIN,
                new RequestAffiliationRequest("athlete-1", "manage"));
        assertThat(req.status()).isEqualTo("PENDING");
        assertThat(req.permission()).isEqualTo("MANAGE");

        assertThatThrownBy(() -> service.decideAffiliation(req.id(), OWNER, true)).isInstanceOf(SecurityException.class);

        AthleteAffiliationDto approved = service.decideAffiliation(req.id(), "athlete-1", true);
        assertThat(approved.status()).isEqualTo("ACTIVE");
        verify(events).publishEvent(any(OrganizationAthleteAffiliatedEvent.class));

        service.endAffiliation(req.id(), "athlete-1", List.of("ATHLETE"));
        verify(events).publishEvent(any(OrganizationAthleteAffiliationEndedEvent.class));
    }

    @Test
    void verifyTwiceIsConflict() {
        OrganizationDto org = createOrg();
        service.verify(org.id(), "admin-1");
        assertThatThrownBy(() -> service.verify(org.id(), "admin-1")).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(() -> service.reject(org.id(), "admin-1", "x")).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void ownerCannotBeRemoved() {
        OrganizationDto org = createOrg();
        assertThatThrownBy(() -> service.removeMember(org.id(), OWNER, OWNER, NO_ADMIN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(Optional.ofNullable(org.id())).isPresent();
    }
}
