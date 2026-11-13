package com.jovine360.talenthub.safeguarding.domain.repository;

import com.jovine360.talenthub.safeguarding.domain.model.ConsentRecord;
import com.jovine360.talenthub.safeguarding.domain.model.ConsentPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, String> {
    Optional<ConsentRecord> findByAthleteIdAndPurposeCodeAndRevokedAtIsNull(
            String athleteId, ConsentPurpose purposeCode);
    List<ConsentRecord> findByAthleteId(String athleteId);
}
