package com.jovine360.talenthub.safeguarding.application.service;

import com.jovine360.talenthub.safeguarding.domain.model.DataSubjectRequest;
import com.jovine360.talenthub.safeguarding.domain.repository.DataSubjectRequestRepository;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Data subject requests (access, deletion, correction). Users file them; an administrator
 * records the outcome after the data protection officer has handled it offline.
 */
@Service
@Transactional
public class DataSubjectRequestService {

    static final Set<String> TYPES = Set.of("ACCESS", "DELETION", "CORRECTION");
    static final Set<String> OUTCOMES = Set.of("COMPLETED", "REJECTED");

    private final DataSubjectRequestRepository repository;

    public DataSubjectRequestService(DataSubjectRequestRepository repository) {
        this.repository = repository;
    }

    public DataSubjectRequest submit(String requesterId, String requestType) {
        String type = requestType.toUpperCase();
        if (!TYPES.contains(type)) {
            throw new IllegalArgumentException("requestType must be ACCESS, DELETION or CORRECTION");
        }
        boolean open = repository.findByRequesterIdOrderBySubmittedAtDesc(requesterId).stream()
                .anyMatch(r -> r.getRequestType().equals(type) && "PENDING".equals(r.getStatus()));
        if (open) {
            throw new DuplicateResourceException("You already have a pending " + type + " request");
        }
        DataSubjectRequest r = new DataSubjectRequest();
        r.setRequesterId(requesterId);
        r.setRequestType(type);
        r.setStatus("PENDING");
        r.setSubmittedAt(Instant.now());
        return repository.save(r);
    }

    @Transactional(readOnly = true)
    public List<DataSubjectRequest> mine(String requesterId) {
        return repository.findByRequesterIdOrderBySubmittedAtDesc(requesterId);
    }

    @Transactional(readOnly = true)
    public List<DataSubjectRequest> list(String status) {
        return status == null || status.isBlank()
                ? repository.findAllByOrderBySubmittedAtDesc()
                : repository.findByStatusOrderBySubmittedAtAsc(status.toUpperCase());
    }

    public DataSubjectRequest resolve(String id, String outcome) {
        String o = outcome.toUpperCase();
        if (!OUTCOMES.contains(o)) {
            throw new IllegalArgumentException("outcome must be COMPLETED or REJECTED");
        }
        DataSubjectRequest r = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DataSubjectRequest", id));
        if (!"PENDING".equals(r.getStatus())) {
            throw new InvalidStateTransitionException("DataSubjectRequest", r.getStatus(), o);
        }
        r.setStatus(o);
        r.setResolvedAt(Instant.now());
        return repository.save(r);
    }
}
