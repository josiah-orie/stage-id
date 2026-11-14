package com.jovine360.talenthub.media.infrastructure.web;

import com.jovine360.talenthub.media.application.service.MediaAssetService;
import com.jovine360.talenthub.media.domain.model.MediaAsset;
import com.jovine360.talenthub.media.domain.model.ModerationStatus;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.domain.service.AuditTrail;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/media")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminMediaController {

    private final MediaAssetService mediaAssetService;
    private final AuditTrail audit;

    public AdminMediaController(MediaAssetService mediaAssetService, AuditTrail audit) {
        this.mediaAssetService = mediaAssetService;
        this.audit = audit;
    }

    record ModerationRequest(@Size(max = 500) String reason) {}

    /** Moderation queue. {@code status} defaults to FLAGGED; RESTRICTED lists hidden assets. */
    @GetMapping("/flagged")
    public ResponseEntity<PageResponse<MediaAsset>> flagged(
            @RequestParam(defaultValue = "FLAGGED") ModerationStatus status, Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(mediaAssetService.listByModerationStatus(status, pageable)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<MediaAsset> approve(@PathVariable String id,
            @RequestBody(required = false) ModerationRequest body,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        String reason = body != null ? body.reason() : null;
        MediaAsset result = mediaAssetService.moderate(id, ModerationStatus.CLEAN, reason, principal.userId());
        audit.record(principal.userId(), principal.roles(), "MEDIA_APPROVED", "MediaAsset", id, reason);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/restrict")
    public ResponseEntity<MediaAsset> restrict(@PathVariable String id,
            @RequestBody(required = false) ModerationRequest body,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        String reason = body != null ? body.reason() : null;
        MediaAsset result = mediaAssetService.moderate(id, ModerationStatus.RESTRICTED, reason, principal.userId());
        audit.record(principal.userId(), principal.roles(), "MEDIA_RESTRICTED", "MediaAsset", id, reason);
        return ResponseEntity.ok(result);
    }
}
