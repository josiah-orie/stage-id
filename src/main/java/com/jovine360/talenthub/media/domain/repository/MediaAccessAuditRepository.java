package com.jovine360.talenthub.media.domain.repository;

import com.jovine360.talenthub.media.domain.model.MediaAccessAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAccessAuditRepository extends JpaRepository<MediaAccessAudit, String> {}
