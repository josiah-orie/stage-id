package com.jovine360.talenthub.guardian.application.service;

import com.jovine360.talenthub.guardian.api.GuardianLinkDto;
import com.jovine360.talenthub.guardian.domain.model.GuardianLink;
import com.jovine360.talenthub.guardian.domain.model.GuardianLinkStatus;
import com.jovine360.talenthub.guardian.domain.repository.GuardianLinkRepository;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class GuardianService {

    private final GuardianLinkRepository guardianLinkRepository;

    public GuardianService(GuardianLinkRepository guardianLinkRepository) {
        this.guardianLinkRepository = guardianLinkRepository;
    }

    public GuardianLinkDto requestLink(String guardianUserId, String athleteUserId, String relationship) {
        if (guardianLinkRepository.existsByGuardianUserIdAndAthleteUserIdAndStatus(
                guardianUserId, athleteUserId, GuardianLinkStatus.ACTIVE)) {
            throw new DuplicateResourceException("GuardianLink", "guardian+athlete", guardianUserId + "+" + athleteUserId);
        }
        GuardianLink link = new GuardianLink();
        link.setGuardianUserId(guardianUserId);
        link.setAthleteUserId(athleteUserId);
        link.setRelationship(relationship);
        link.setStatus(GuardianLinkStatus.PENDING);
        return toDto(guardianLinkRepository.save(link));
    }

    public GuardianLinkDto approveLink(String linkId, String athleteUserId) {
        GuardianLink link = guardianLinkRepository.findById(linkId)
                .orElseThrow(() -> new EntityNotFoundException("GuardianLink", linkId));
        if (!link.getAthleteUserId().equals(athleteUserId)) {
            throw new SecurityException("Not authorised to approve this link");
        }
        link.setStatus(GuardianLinkStatus.ACTIVE);
        link.setConsentGiven(true);
        link.setConsentGivenAt(Instant.now());
        return toDto(guardianLinkRepository.save(link));
    }

    @Transactional(readOnly = true)
    public List<GuardianLinkDto> getLinksForGuardian(String guardianUserId) {
        return guardianLinkRepository
                .findByGuardianUserIdAndStatus(guardianUserId, GuardianLinkStatus.ACTIVE)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<GuardianLinkDto> getLinksForAthlete(String athleteUserId) {
        return guardianLinkRepository
                .findByAthleteUserIdAndStatus(athleteUserId, GuardianLinkStatus.ACTIVE)
                .stream().map(this::toDto).toList();
    }

    private GuardianLinkDto toDto(GuardianLink l) {
        return new GuardianLinkDto(l.getId(), l.getGuardianUserId(), l.getAthleteUserId(),
                l.getRelationship(), l.getStatus().name(), l.isConsentGiven());
    }
}
