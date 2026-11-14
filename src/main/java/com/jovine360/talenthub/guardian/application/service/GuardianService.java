package com.jovine360.talenthub.guardian.application.service;

import com.jovine360.talenthub.guardian.api.CreateGuardianRelationshipRequest;
import com.jovine360.talenthub.guardian.api.GuardianApi;
import com.jovine360.talenthub.guardian.api.GuardianLinkActivatedEvent;
import com.jovine360.talenthub.guardian.api.GuardianLinkDto;
import com.jovine360.talenthub.guardian.api.GuardianLinkEndedEvent;
import com.jovine360.talenthub.guardian.domain.model.GuardianLink;
import com.jovine360.talenthub.guardian.domain.model.GuardianLinkStatus;
import com.jovine360.talenthub.guardian.domain.model.GuardianRelationshipType;
import com.jovine360.talenthub.guardian.domain.repository.GuardianLinkRepository;
import com.jovine360.talenthub.guardian.domain.service.SafeguardingPolicyService;
import com.jovine360.talenthub.identity.api.UserDirectoryApi;
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
import java.util.List;
import java.util.Set;

/**
 * Guardian, parent and agent relationships with an athlete.
 *
 * <p>Lifecycle: PARENT and AGENT links start {@code PENDING} and are approved by the athlete.
 * LEGAL_GUARDIAN links start {@code PENDING_ADMIN_VERIFICATION} and are approved by an
 * administrator, then expire after one year. Either party (or an admin) may revoke.
 */
@Service
@Transactional
public class GuardianService implements GuardianApi {

    static final Set<String> AGENT_PERMISSIONS =
            Set.of("VIEW_PROFILE", "VIEW_STATS", "VIEW_DOCUMENTS", "CONTACT", "MANAGE");
    static final String DEFAULT_AGENT_PERMISSION = "VIEW_STATS";

    private final GuardianLinkRepository linkRepository;
    private final SafeguardingPolicyService safeguardingPolicy;
    private final UserDirectoryApi userDirectory;
    private final ApplicationEventPublisher eventPublisher;

    public GuardianService(GuardianLinkRepository linkRepository,
                           SafeguardingPolicyService safeguardingPolicy,
                           UserDirectoryApi userDirectory,
                           ApplicationEventPublisher eventPublisher) {
        this.linkRepository = linkRepository;
        this.safeguardingPolicy = safeguardingPolicy;
        this.userDirectory = userDirectory;
        this.eventPublisher = eventPublisher;
    }

    public GuardianLinkDto requestLink(String actorId, Collection<String> actorRoles,
                                       CreateGuardianRelationshipRequest req) {
        GuardianRelationshipType type = req.relationshipType();
        String requiredRole = requiredRole(type);
        if (actorRoles == null || !actorRoles.contains(requiredRole)) {
            throw new SecurityException("The " + requiredRole + " role is required to request a "
                    + type + " relationship");
        }
        if (actorId.equals(req.athleteId())) {
            throw new IllegalArgumentException("You cannot create a relationship with yourself");
        }
        if (!userDirectory.userHasRole(req.athleteId(), "ATHLETE")) {
            throw new EntityNotFoundException("Athlete", req.athleteId());
        }
        boolean documentMissing = req.verificationDocumentRef() == null || req.verificationDocumentRef().isBlank();
        if (type == GuardianRelationshipType.LEGAL_GUARDIAN && documentMissing) {
            throw new IllegalArgumentException("verificationDocumentRef is required for a LEGAL_GUARDIAN relationship");
        }
        String permission = null;
        if (type == GuardianRelationshipType.AGENT) {
            permission = req.permission() == null ? DEFAULT_AGENT_PERMISSION : req.permission().toUpperCase();
            if (!AGENT_PERMISSIONS.contains(permission)) {
                throw new IllegalArgumentException("Unknown permission: " + req.permission());
            }
        }
        boolean duplicate = linkRepository.findByGuardianUserIdAndAthleteUserId(actorId, req.athleteId()).stream()
                .anyMatch(l -> l.getRelationshipType() == type && isOpen(l));
        if (duplicate) {
            throw new DuplicateResourceException("A " + type + " relationship with this athlete already exists");
        }

        GuardianLink link = new GuardianLink();
        link.setGuardianUserId(actorId);
        link.setAthleteUserId(req.athleteId());
        link.setRelationshipType(type);
        link.setRelationship(req.relationship() != null ? req.relationship() : type.name());
        link.setPermission(permission);
        link.setVerificationDocumentRef(documentMissing ? null : req.verificationDocumentRef());
        link.setStatus(type == GuardianRelationshipType.LEGAL_GUARDIAN
                ? GuardianLinkStatus.PENDING_ADMIN_VERIFICATION : GuardianLinkStatus.PENDING);
        return toDto(linkRepository.save(link));
    }

    /** The athlete approves a PENDING link, or an admin approves a PENDING_ADMIN_VERIFICATION link. */
    public GuardianLinkDto approve(String linkId, String actorId, Collection<String> actorRoles) {
        GuardianLink link = load(linkId);
        assertCanDecide(link, actorId, actorRoles, GuardianLinkStatus.ACTIVE);
        Instant now = Instant.now();
        link.setStatus(GuardianLinkStatus.ACTIVE);
        link.setConsentGiven(true);
        link.setConsentGivenAt(now);
        if (link.getRelationshipType() == GuardianRelationshipType.LEGAL_GUARDIAN) {
            link.setVerifiedBy(actorId);
            link.setVerifiedAt(now);
            link.setExpiresAt(now.plus(365, ChronoUnit.DAYS));
        }
        GuardianLink saved = linkRepository.save(link);
        eventPublisher.publishEvent(new GuardianLinkActivatedEvent(saved.getId(), saved.getGuardianUserId(),
                saved.getAthleteUserId(), saved.getRelationshipType().name(), saved.getPermission(),
                saved.getExpiresAt()));
        return toDto(saved);
    }

    public GuardianLinkDto reject(String linkId, String actorId, Collection<String> actorRoles, String reason) {
        GuardianLink link = load(linkId);
        assertCanDecide(link, actorId, actorRoles, GuardianLinkStatus.REJECTED);
        boolean adminReview = link.getStatus() == GuardianLinkStatus.PENDING_ADMIN_VERIFICATION;
        link.setStatus(GuardianLinkStatus.REJECTED);
        link.setRejectionReason(reason);
        if (adminReview) {
            link.setVerifiedBy(actorId);
            link.setVerifiedAt(Instant.now());
        }
        return toDto(linkRepository.save(link));
    }

    /** Either party or an admin ends a pending or active relationship. */
    public GuardianLinkDto revoke(String linkId, String actorId, Collection<String> actorRoles) {
        GuardianLink link = load(linkId);
        boolean party = actorId.equals(link.getGuardianUserId()) || actorId.equals(link.getAthleteUserId());
        if (!party && !isAdmin(actorRoles)) {
            throw new SecurityException("Not authorised to revoke this relationship");
        }
        if (link.getStatus() != GuardianLinkStatus.ACTIVE
                && link.getStatus() != GuardianLinkStatus.PENDING
                && link.getStatus() != GuardianLinkStatus.PENDING_ADMIN_VERIFICATION) {
            throw new InvalidStateTransitionException("GuardianLink", link.getStatus().name(), "REVOKED");
        }
        boolean wasActive = link.getStatus() == GuardianLinkStatus.ACTIVE;
        link.setStatus(GuardianLinkStatus.REVOKED);
        link.setRevokedAt(Instant.now());
        GuardianLink saved = linkRepository.save(link);
        if (wasActive) {
            eventPublisher.publishEvent(new GuardianLinkEndedEvent(saved.getId(), saved.getGuardianUserId(),
                    saved.getAthleteUserId(), saved.getRelationshipType().name()));
        }
        return toDto(saved);
    }

    /** Every relationship the caller is part of, as requester or as athlete. */
    @Transactional(readOnly = true)
    public List<GuardianLinkDto> listMine(String userId) {
        return linkRepository.findByGuardianUserIdOrAthleteUserIdOrderByCreatedAtDesc(userId, userId)
                .stream().map(this::toDto).toList();
    }

    /** Athletes the caller currently manages as PARENT or LEGAL_GUARDIAN. */
    @Transactional(readOnly = true)
    public List<GuardianLinkDto> listGuardedAthletes(String guardianUserId) {
        return linkRepository.findByGuardianUserIdAndStatus(guardianUserId, GuardianLinkStatus.ACTIVE).stream()
                .filter(GuardianLink::isEffectivelyActive)
                .filter(GuardianLink::isGuardianLevel)
                .map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<GuardianLinkDto> listByStatus(GuardianLinkStatus status, Pageable pageable) {
        return linkRepository.findByStatus(status, pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveGuardianLink(String guardianUserId, String athleteUserId) {
        return linkRepository.findByGuardianUserIdAndAthleteUserId(guardianUserId, athleteUserId).stream()
                .anyMatch(l -> l.isEffectivelyActive() && l.isGuardianLevel());
    }

    @Override
    @Transactional(readOnly = true)
    public void assertGuardianConsentForMinor(String athleteUserId) {
        safeguardingPolicy.assertGuardianConsent(athleteUserId);
    }

    // ---- helpers ---------------------------------------------------------------------------

    private void assertCanDecide(GuardianLink link, String actorId, Collection<String> actorRoles,
                                 GuardianLinkStatus target) {
        switch (link.getStatus()) {
            case PENDING -> {
                if (!actorId.equals(link.getAthleteUserId())) {
                    throw new SecurityException("Only the athlete can decide on this relationship");
                }
            }
            case PENDING_ADMIN_VERIFICATION -> {
                if (!isAdmin(actorRoles)) {
                    throw new SecurityException("Only an administrator can decide on this relationship");
                }
            }
            default -> throw new InvalidStateTransitionException("GuardianLink", link.getStatus().name(), target.name());
        }
    }

    private static boolean isOpen(GuardianLink l) {
        return l.getStatus() == GuardianLinkStatus.PENDING
                || l.getStatus() == GuardianLinkStatus.PENDING_ADMIN_VERIFICATION
                || l.isEffectivelyActive();
    }

    private static boolean isAdmin(Collection<String> roles) {
        return roles != null && (roles.contains("ADMIN") || roles.contains("SUPER_ADMIN"));
    }

    private static String requiredRole(GuardianRelationshipType type) {
        return switch (type) {
            case PARENT -> "PARENT";
            case LEGAL_GUARDIAN -> "GUARDIAN";
            case AGENT -> "AGENT";
        };
    }

    private GuardianLink load(String linkId) {
        return linkRepository.findById(linkId)
                .orElseThrow(() -> new EntityNotFoundException("GuardianLink", linkId));
    }

    private GuardianLinkDto toDto(GuardianLink l) {
        String status = l.getStatus() == GuardianLinkStatus.ACTIVE && l.isExpired()
                ? "EXPIRED" : l.getStatus().name();
        return new GuardianLinkDto(l.getId(), l.getGuardianUserId(), l.getAthleteUserId(),
                l.getRelationshipType().name(), l.getRelationship(), l.getPermission(), status,
                l.isConsentGiven(), l.getVerificationDocumentRef(), l.getCreatedAt(),
                l.getConsentGivenAt(), l.getExpiresAt(), l.getRevokedAt());
    }
}
