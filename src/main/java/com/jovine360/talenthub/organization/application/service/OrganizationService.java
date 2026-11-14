package com.jovine360.talenthub.organization.application.service;

import com.jovine360.talenthub.identity.api.UserDirectoryApi;
import com.jovine360.talenthub.identity.api.UserRoleGrantApi;
import com.jovine360.talenthub.organization.api.*;
import com.jovine360.talenthub.organization.domain.model.*;
import com.jovine360.talenthub.organization.domain.repository.OrganizationAthleteRelationshipRepository;
import com.jovine360.talenthub.organization.domain.repository.OrganizationInvitationRepository;
import com.jovine360.talenthub.organization.domain.repository.OrganizationMemberRepository;
import com.jovine360.talenthub.organization.domain.repository.OrganizationRepository;
import com.jovine360.talenthub.organization.domain.service.OrgCodeGeneratorService;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Organizations, their members, invitations and athlete affiliations.
 *
 * <p>Creating an organization makes the caller its OWNER member and grants them the ORG_ADMIN
 * platform role. New organizations start PENDING_VERIFICATION. Invitations and affiliation
 * requests are only allowed once an administrator has verified the organization (ACTIVE).
 */
@Service
@Transactional
public class OrganizationService implements OrganizationApi {

    static final Set<String> PERMISSIONS =
            Set.of("VIEW_PROFILE", "VIEW_STATS", "VIEW_DOCUMENTS", "CONTACT", "MANAGE");
    private static final Set<OrgMemberRole> ATHLETE_WORK_ROLES =
            EnumSet.of(OrgMemberRole.OWNER, OrgMemberRole.ADMIN, OrgMemberRole.COACH, OrgMemberRole.ATHLETE_MANAGER);
    private static final long INVITATION_DAYS = 7;
    private static final String ACTIVE = "ACTIVE";

    private final OrganizationRepository orgRepository;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationInvitationRepository invitationRepository;
    private final OrganizationAthleteRelationshipRepository affiliationRepository;
    private final OrgCodeGeneratorService codeGenerator;
    private final UserRoleGrantApi roleGrantApi;
    private final UserDirectoryApi userDirectory;
    private final ApplicationEventPublisher eventPublisher;

    public OrganizationService(OrganizationRepository orgRepository,
                               OrganizationMemberRepository memberRepository,
                               OrganizationInvitationRepository invitationRepository,
                               OrganizationAthleteRelationshipRepository affiliationRepository,
                               OrgCodeGeneratorService codeGenerator,
                               UserRoleGrantApi roleGrantApi,
                               UserDirectoryApi userDirectory,
                               ApplicationEventPublisher eventPublisher) {
        this.orgRepository = orgRepository;
        this.memberRepository = memberRepository;
        this.invitationRepository = invitationRepository;
        this.affiliationRepository = affiliationRepository;
        this.codeGenerator = codeGenerator;
        this.roleGrantApi = roleGrantApi;
        this.userDirectory = userDirectory;
        this.eventPublisher = eventPublisher;
    }

    // ---- organizations --------------------------------------------------------------------

    public OrganizationDto create(String creatorUserId, CreateOrganizationRequest req) {
        Organization org = new Organization();
        org.setName(req.name());
        org.setOrgType(req.orgType());
        org.setCountryCode(req.countryCode());
        org.setCity(req.city());
        org.setDescription(req.description());
        org.setAdminUserId(creatorUserId);
        org.setCode(codeGenerator.generate(req.name()));
        org.setStatus(OrgStatus.PENDING_VERIFICATION);
        Organization saved = orgRepository.save(org);

        addMember(saved.getId(), creatorUserId, OrgMemberRole.OWNER);
        roleGrantApi.grantRole(creatorUserId, "ORG_ADMIN");
        return toDto(saved);
    }

    public OrganizationDto update(String orgId, String actorId, Collection<String> actorRoles,
                                  UpdateOrganizationRequest req) {
        Organization org = load(orgId);
        assertCanManage(orgId, actorId, actorRoles);
        if (req.name() != null) org.setName(req.name());
        if (req.countryCode() != null) org.setCountryCode(req.countryCode());
        if (req.city() != null) org.setCity(req.city());
        if (req.description() != null) org.setDescription(req.description());
        if (req.logoUrl() != null) org.setLogoUrl(req.logoUrl());
        return toDto(orgRepository.save(org));
    }

    @Transactional(readOnly = true)
    public OrganizationDto getById(String id) {
        return toDto(load(id));
    }

    /** Organizations the user is an active member of. */
    @Transactional(readOnly = true)
    public List<OrganizationDto> getMyOrganizations(String userId) {
        return memberRepository.findByUserIdAndStatus(userId, ACTIVE).stream()
                .map(m -> orgRepository.findById(m.getOrganizationId()))
                .flatMap(Optional::stream)
                .map(this::toDto)
                .toList();
    }

    // ---- verification (admin) -------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<OrganizationDto> listByStatus(OrgStatus status, Pageable pageable) {
        return orgRepository.findByStatus(status, pageable).map(this::toDto);
    }

    public OrganizationDto verify(String orgId, String adminId) {
        Organization org = load(orgId);
        if (org.getStatus() != OrgStatus.PENDING_VERIFICATION) {
            throw new InvalidStateTransitionException("Organization", org.getStatus().name(), "ACTIVE");
        }
        org.setStatus(OrgStatus.ACTIVE);
        org.setVerifiedBy(adminId);
        org.setVerifiedAt(Instant.now());
        org.setRejectionReason(null);
        return toDto(orgRepository.save(org));
    }

    public OrganizationDto reject(String orgId, String adminId, String reason) {
        Organization org = load(orgId);
        if (org.getStatus() != OrgStatus.PENDING_VERIFICATION) {
            throw new InvalidStateTransitionException("Organization", org.getStatus().name(), "DEACTIVATED");
        }
        org.setStatus(OrgStatus.DEACTIVATED);
        org.setVerifiedBy(adminId);
        org.setVerifiedAt(Instant.now());
        org.setRejectionReason(reason);
        return toDto(orgRepository.save(org));
    }

    // ---- members & invitations ------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<OrganizationMemberDto> listMembers(String orgId, String actorId, Collection<String> actorRoles) {
        load(orgId);
        assertCanRead(orgId, actorId, actorRoles);
        return memberRepository.findByOrganizationIdAndStatus(orgId, ACTIVE).stream().map(this::toDto).toList();
    }

    public InvitationDto invite(String orgId, String actorId, Collection<String> actorRoles, InviteMemberRequest req) {
        Organization org = load(orgId);
        assertCanManage(orgId, actorId, actorRoles);
        assertActive(org);
        if (req.memberRole() == OrgMemberRole.OWNER) {
            throw new IllegalArgumentException("An organization has exactly one OWNER, who cannot be invited");
        }
        String email = req.email().toLowerCase().trim();
        boolean pending = invitationRepository.findByOrganizationIdAndStatus(orgId, "PENDING").stream()
                .anyMatch(i -> i.getEmail().equals(email) && !i.isExpired());
        if (pending) {
            throw new DuplicateResourceException("A pending invitation already exists for " + email);
        }
        OrganizationInvitation inv = new OrganizationInvitation();
        inv.setOrganizationId(orgId);
        inv.setEmail(email);
        inv.setMemberRole(req.memberRole());
        inv.setToken(UUID.randomUUID().toString());
        inv.setInvitedBy(actorId);
        inv.setExpiresAt(Instant.now().plus(INVITATION_DAYS, ChronoUnit.DAYS));
        OrganizationInvitation saved = invitationRepository.save(inv);
        return toDto(saved, true);
    }

    @Transactional(readOnly = true)
    public List<InvitationDto> listPendingInvitations(String orgId, String actorId, Collection<String> actorRoles) {
        load(orgId);
        assertCanManage(orgId, actorId, actorRoles);
        return invitationRepository.findByOrganizationIdAndStatus(orgId, "PENDING").stream()
                .map(i -> toDto(i, false)).toList();
    }

    /** The invitee accepts. The invitation's email must match the caller's account email. */
    public OrganizationMemberDto acceptInvitation(String orgId, String actorId, String actorEmail, String token) {
        OrganizationInvitation inv = invitationRepository.findByToken(token)
                .filter(i -> i.getOrganizationId().equals(orgId))
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));
        if (!"PENDING".equals(inv.getStatus())) {
            throw new InvalidStateTransitionException("Invitation", inv.getStatus(), "ACCEPTED");
        }
        if (inv.isExpired()) {
            throw new InvalidStateTransitionException("Invitation", "EXPIRED", "ACCEPTED");
        }
        if (actorEmail == null || !inv.getEmail().equalsIgnoreCase(actorEmail)) {
            throw new SecurityException("This invitation was sent to a different email address");
        }
        if (memberRepository.findByOrganizationIdAndUserIdAndStatus(orgId, actorId, ACTIVE).isPresent()) {
            throw new DuplicateResourceException("You are already a member of this organization");
        }
        inv.setStatus("ACCEPTED");
        inv.setAcceptedBy(actorId);
        inv.setAcceptedAt(Instant.now());
        invitationRepository.save(inv);
        if (inv.getMemberRole().canManageOrganization()) {
            roleGrantApi.grantRole(actorId, "ORG_ADMIN");
        }
        return toDto(addMember(orgId, actorId, inv.getMemberRole()));
    }

    public void removeMember(String orgId, String memberUserId, String actorId, Collection<String> actorRoles) {
        load(orgId);
        boolean self = memberUserId.equals(actorId);
        if (!self) assertCanManage(orgId, actorId, actorRoles);
        OrganizationMember m = memberRepository.findByOrganizationIdAndUserIdAndStatus(orgId, memberUserId, ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("OrganizationMember", memberUserId));
        if (m.getMemberRole() == OrgMemberRole.OWNER) {
            throw new IllegalArgumentException("The organization OWNER cannot be removed");
        }
        m.setStatus("REMOVED");
        m.setRemovedAt(Instant.now());
        memberRepository.save(m);
    }

    // ---- athlete affiliations -------------------------------------------------------------

    public AthleteAffiliationDto requestAffiliation(String orgId, String actorId, Collection<String> actorRoles,
                                                    RequestAffiliationRequest req) {
        Organization org = load(orgId);
        assertCanManageAthletes(orgId, actorId, actorRoles);
        assertActive(org);
        if (!userDirectory.userHasRole(req.athleteId(), "ATHLETE")) {
            throw new EntityNotFoundException("Athlete", req.athleteId());
        }
        String permission = req.permission() == null ? "VIEW_STATS" : req.permission().toUpperCase();
        if (!PERMISSIONS.contains(permission)) {
            throw new IllegalArgumentException("Unknown permission: " + req.permission());
        }
        if (!affiliationRepository.findByOrganizationIdAndAthleteUserIdAndStatusIn(
                orgId, req.athleteId(), List.of("PENDING", ACTIVE)).isEmpty()) {
            throw new DuplicateResourceException("This athlete already has a pending or active affiliation with the organization");
        }
        OrganizationAthleteRelationship rel = new OrganizationAthleteRelationship();
        rel.setOrganizationId(orgId);
        rel.setAthleteUserId(req.athleteId());
        rel.setPermission(permission);
        rel.setRequestedBy(actorId);
        rel.setStatus("PENDING");
        return toDto(affiliationRepository.save(rel));
    }

    @Transactional(readOnly = true)
    public List<AthleteAffiliationDto> listAthletes(String orgId, String status, String actorId,
                                                    Collection<String> actorRoles) {
        load(orgId);
        assertCanRead(orgId, actorId, actorRoles);
        return affiliationRepository.findByOrganizationIdAndStatus(orgId, status).stream().map(this::toDto).toList();
    }

    /** Affiliations for the calling athlete (any status), newest first. */
    @Transactional(readOnly = true)
    public List<AthleteAffiliationDto> listMyAffiliations(String athleteUserId) {
        return affiliationRepository.findByAthleteUserIdOrderByCreatedAtDesc(athleteUserId).stream()
                .map(this::toDto).toList();
    }

    public AthleteAffiliationDto decideAffiliation(String relationshipId, String actorId, boolean approve) {
        OrganizationAthleteRelationship rel = loadAffiliation(relationshipId);
        if (!rel.getAthleteUserId().equals(actorId)) {
            throw new SecurityException("Only the athlete can decide on this affiliation");
        }
        if (!"PENDING".equals(rel.getStatus())) {
            throw new InvalidStateTransitionException("Affiliation", rel.getStatus(), approve ? ACTIVE : "REJECTED");
        }
        rel.setStatus(approve ? ACTIVE : "REJECTED");
        rel.setDecidedAt(Instant.now());
        OrganizationAthleteRelationship saved = affiliationRepository.save(rel);
        if (approve) {
            eventPublisher.publishEvent(new OrganizationAthleteAffiliatedEvent(saved.getId(),
                    saved.getOrganizationId(), saved.getAthleteUserId(), saved.getPermission()));
        }
        return toDto(saved);
    }

    /** The athlete or an organization admin ends a pending or active affiliation. */
    public AthleteAffiliationDto endAffiliation(String relationshipId, String actorId, Collection<String> actorRoles) {
        OrganizationAthleteRelationship rel = loadAffiliation(relationshipId);
        boolean athlete = rel.getAthleteUserId().equals(actorId);
        if (!athlete) assertCanManageAthletes(rel.getOrganizationId(), actorId, actorRoles);
        if (!"PENDING".equals(rel.getStatus()) && !ACTIVE.equals(rel.getStatus())) {
            throw new InvalidStateTransitionException("Affiliation", rel.getStatus(), "ENDED");
        }
        boolean wasActive = ACTIVE.equals(rel.getStatus());
        rel.setStatus("ENDED");
        rel.setEndedAt(Instant.now());
        OrganizationAthleteRelationship saved = affiliationRepository.save(rel);
        if (wasActive) {
            eventPublisher.publishEvent(new OrganizationAthleteAffiliationEndedEvent(saved.getId(),
                    saved.getOrganizationId(), saved.getAthleteUserId()));
        }
        return toDto(saved);
    }

    // ---- OrganizationApi ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Optional<OrganizationDto> findById(String orgId) {
        return orgRepository.findById(orgId).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrganizationDto> findByCode(String code) {
        return orgRepository.findByCode(code).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean orgExists(String orgId) {
        return orgRepository.existsById(orgId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(String orgId, String userId) {
        return activeMembership(orgId, userId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrganizationAdmin(String orgId, String userId) {
        return activeMembership(orgId, userId).map(m -> m.getMemberRole().canManageOrganization()).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> organizationsWithAthleteAccess(String userId) {
        return memberRepository.findByUserIdAndStatus(userId, ACTIVE).stream()
                .filter(m -> ATHLETE_WORK_ROLES.contains(m.getMemberRole()))
                .map(OrganizationMember::getOrganizationId)
                .toList();
    }

    // ---- helpers --------------------------------------------------------------------------

    private OrganizationMember addMember(String orgId, String userId, OrgMemberRole role) {
        OrganizationMember m = new OrganizationMember();
        m.setOrganizationId(orgId);
        m.setUserId(userId);
        m.setMemberRole(role);
        m.setStatus(ACTIVE);
        m.setJoinedAt(Instant.now());
        return memberRepository.save(m);
    }

    private Optional<OrganizationMember> activeMembership(String orgId, String userId) {
        return memberRepository.findByOrganizationIdAndUserIdAndStatus(orgId, userId, ACTIVE);
    }

    private static boolean isPlatformAdmin(Collection<String> roles) {
        return roles != null && (roles.contains("ADMIN") || roles.contains("SUPER_ADMIN"));
    }

    private void assertCanRead(String orgId, String actorId, Collection<String> roles) {
        if (!isPlatformAdmin(roles) && !isMember(orgId, actorId)) {
            throw new SecurityException("Only members of this organization can see this");
        }
    }

    private void assertCanManage(String orgId, String actorId, Collection<String> roles) {
        if (!isPlatformAdmin(roles) && !isOrganizationAdmin(orgId, actorId)) {
            throw new SecurityException("Only the organization's OWNER or ADMIN can do this");
        }
    }

    private void assertCanManageAthletes(String orgId, String actorId, Collection<String> roles) {
        boolean allowed = isPlatformAdmin(roles) || activeMembership(orgId, actorId)
                .map(m -> m.getMemberRole().canManageAthletes()).orElse(false);
        if (!allowed) {
            throw new SecurityException("Only the organization's OWNER, ADMIN or ATHLETE_MANAGER can do this");
        }
    }

    private static void assertActive(Organization org) {
        if (org.getStatus() == OrgStatus.PENDING_VERIFICATION) {
            throw new SecurityException("Organization is pending verification");
        }
        if (org.getStatus() != OrgStatus.ACTIVE) {
            throw new SecurityException("Organization is " + org.getStatus());
        }
    }

    private Organization load(String id) {
        return orgRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Organization", id));
    }

    private OrganizationAthleteRelationship loadAffiliation(String id) {
        return affiliationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OrganizationAthleteRelationship", id));
    }

    private OrganizationDto toDto(Organization o) {
        return new OrganizationDto(o.getId(), o.getCode(), o.getName(),
                o.getOrgType() != null ? o.getOrgType().name() : null,
                o.getCountryCode(), o.getCity(), o.getAdminUserId(),
                o.getLogoUrl(), o.getStatus() != null ? o.getStatus().name() : null,
                o.getDescription(), o.getVerifiedAt(), o.getRejectionReason());
    }

    private OrganizationMemberDto toDto(OrganizationMember m) {
        return new OrganizationMemberDto(m.getId(), m.getOrganizationId(), m.getUserId(),
                m.getMemberRole().name(), m.getStatus(), m.getJoinedAt());
    }

    private InvitationDto toDto(OrganizationInvitation i, boolean includeToken) {
        String status = "PENDING".equals(i.getStatus()) && i.isExpired() ? "EXPIRED" : i.getStatus();
        return new InvitationDto(i.getId(), i.getOrganizationId(), i.getEmail(), i.getMemberRole().name(),
                status, includeToken ? i.getToken() : null, i.getExpiresAt());
    }

    private AthleteAffiliationDto toDto(OrganizationAthleteRelationship r) {
        return new AthleteAffiliationDto(r.getId(), r.getOrganizationId(), r.getAthleteUserId(), r.getPermission(),
                r.getStatus(), r.getRequestedBy(), r.getCreatedAt(), r.getDecidedAt(), r.getEndedAt());
    }
}
