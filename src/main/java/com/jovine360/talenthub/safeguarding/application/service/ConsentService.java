package com.jovine360.talenthub.safeguarding.application.service;

import com.jovine360.talenthub.safeguarding.domain.event.AthleteConsentRevokedEvent;
import com.jovine360.talenthub.safeguarding.domain.model.ConsentPurpose;
import com.jovine360.talenthub.safeguarding.domain.model.ConsentRecord;
import com.jovine360.talenthub.safeguarding.domain.repository.ConsentRecordRepository;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class ConsentService {

    private final ConsentRecordRepository consentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ConsentService(ConsentRecordRepository consentRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.consentRepository = consentRepository;
        this.eventPublisher = eventPublisher;
    }

    public ConsentRecord recordConsent(String athleteId, ConsentPurpose purpose,
                                        String consentVersion, String consentedBy,
                                        String authSource) {
        ConsentRecord record = new ConsentRecord();
        record.setAthleteId(athleteId);
        record.setPurposeCode(purpose);
        record.setConsentVersion(consentVersion);
        record.setConsentedBy(consentedBy);
        record.setConsentedAt(Instant.now());
        record.setAuthorizationSource(authSource);
        ConsentRecord saved = consentRepository.save(record);
        eventPublisher.publishEvent(new com.jovine360.talenthub.safeguarding.api.AthleteConsentGrantedEvent(
                athleteId, purpose.name(), consentedBy));
        return saved;
    }

    /** Every consent record for the athlete, newest first, including revoked ones. */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<ConsentRecord> listConsents(String athleteId) {
        return consentRepository.findByAthleteId(athleteId).stream()
                .sorted(java.util.Comparator.comparing(ConsentRecord::getConsentedAt,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                .toList();
    }

    public void revokeConsent(String athleteId, ConsentPurpose purpose, String revokedBy) {
        ConsentRecord record = consentRepository
                .findByAthleteIdAndPurposeCodeAndRevokedAtIsNull(athleteId, purpose)
                .orElseThrow(() -> new EntityNotFoundException("ConsentRecord", athleteId + ":" + purpose));
        record.setRevokedAt(Instant.now());
        record.setRevokedBy(revokedBy);
        consentRepository.save(record);
        eventPublisher.publishEvent(
                new AthleteConsentRevokedEvent(athleteId, purpose.name(), revokedBy));
    }
}
