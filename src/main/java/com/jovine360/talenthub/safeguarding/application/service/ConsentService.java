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
        return consentRepository.save(record);
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
