package com.jovine360.talenthub.athlete.domain.service;

import com.jovine360.talenthub.athlete.domain.model.AthleteNumberSequence;
import com.jovine360.talenthub.athlete.domain.repository.AthleteNumberSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates unique, sequential athlete numbers using SELECT FOR UPDATE.
 *
 * <ul>
 *   <li>Unaffiliated: {@code TH-{LPAD(seq,5,'0')}} e.g. {@code TH-00001}
 *   <li>Org-affiliated: {@code {ORG_CODE}-{SPORT_CODE}-{LPAD(seq,5,'0')}} e.g. {@code FCB-FB-00042}
 * </ul>
 */
@Service
public class AthleteNumberGeneratorService {

    private static final String UNAFFILIATED_PREFIX = "TH";

    private final AthleteNumberSequenceRepository sequenceRepository;

    public AthleteNumberGeneratorService(AthleteNumberSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generates the next athlete number for an unaffiliated athlete.
     * Must be called within an active transaction.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String generateUnaffiliatedNumber() {
        return generate(UNAFFILIATED_PREFIX);
    }

    /**
     * Generates the next athlete number for an organisation-affiliated athlete.
     *
     * @param orgCode   organisation code e.g. "FCB"
     * @param sportCode sport code e.g. "FB"
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String generateAffiliatedNumber(String orgCode, String sportCode) {
        String prefix = orgCode.toUpperCase() + "-" + sportCode.toUpperCase();
        return generate(prefix);
    }

    private String generate(String prefixKey) {
        AthleteNumberSequence seq = sequenceRepository.findByPrefixKeyForUpdate(prefixKey)
                .orElseGet(() -> {
                    AthleteNumberSequence newSeq = new AthleteNumberSequence();
                    newSeq.setPrefixKey(prefixKey);
                    newSeq.setLastSeq(0L);
                    return sequenceRepository.save(newSeq);
                });

        long next = seq.getLastSeq() + 1;
        seq.setLastSeq(next);
        sequenceRepository.save(seq);

        return prefixKey + "-" + String.format("%05d", next);
    }
}
