package com.jovine360.talenthub.media.infrastructure.web;

import com.jovine360.talenthub.media.application.service.MediaAssetService;
import com.jovine360.talenthub.media.domain.model.AssetType;
import com.jovine360.talenthub.media.domain.model.OwnerType;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activities")
public class ActivityMediaController {

    private final MediaAssetService mediaAssetService;

    public ActivityMediaController(MediaAssetService mediaAssetService) {
        this.mediaAssetService = mediaAssetService;
    }

    record AddActivityMediaRequest(@NotBlank String ownerId, @NotBlank String originalFilename,
                                    @NotBlank String mimeType) {}

    @PostMapping("/{id}/media")
    public ResponseEntity<MediaAssetService.InitiateUploadResponse> addMedia(
            @PathVariable String id,
            @Valid @RequestBody AddActivityMediaRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        MediaAssetService.InitiateUploadResponse response = mediaAssetService.initiateUpload(
                req.ownerId(), OwnerType.ATHLETE, AssetType.ACTIVITY_VIDEO,
                req.originalFilename(), req.mimeType(), principal.userId(), principal.roles());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
