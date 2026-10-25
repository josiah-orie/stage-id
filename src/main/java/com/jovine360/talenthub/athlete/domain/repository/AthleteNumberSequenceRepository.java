package com.jovine360.talenthub.athlete.domain.repository;

import com.jovine360.talenthub.athlete.domain.model.AthleteNumberSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AthleteNumberSequenceRepository extends JpaRepository<AthleteNumberSequence, String> {

    /**
     * Acquires a pessimistic write lock on the sequence row before returning it.
     * This guarantees sequential, collision-free athlete numbers.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AthleteNumberSequence s WHERE s.prefixKey = :prefixKey")
    Optional<AthleteNumberSequence> findByPrefixKeyForUpdate(String prefixKey);

    Optional<AthleteNumberSequence> findByPrefixKey(String prefixKey);
}
