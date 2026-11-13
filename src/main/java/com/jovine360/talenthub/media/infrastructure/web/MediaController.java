package com.jovine360.talenthub.media.infrastructure.web;

import com.jovine360.talenthub.media.application.service.MediaAssetService;
import com.jovine360.talenthub.media.domain.model.AssetType;
import com.jovine360.talenthub.media.domain.model.MediaAsset;
import com.jovine360.talenthub.media.domain.model.OwnerType;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaAssetService mediaAssetService;

    public MediaController(MediaAssetService mediaAssetService) {
        this.mediaAssetService = mediaAssetService;
    }

    record InitiateUploadRequest(@NotBlank String ownerId, @NotBlank String ownerType,
                                  @NotBlank String assetType, @NotBlank String originalFilename,
                                  @NotBlank String mimeType) {}

    @PostMapping("/uploads")
    public ResponseEntity<MediaAssetService.InitiateUploadResponse> initiateUpload(
            @Valid @RequestBody InitiateUploadRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        MediaAssetService.InitiateUploadResponse response = mediaAssetService.initiateUpload(
                req.ownerId(),
                OwnerType.valueOf(req.ownerType()),
                AssetType.valueOf(req.assetType()),
                req.originalFilename(),
                req.mimeType(),
                principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/uploads/{id}/complete")
    public ResponseEntity<MediaAsset> completeUpload(@PathVariable String id) {
        return ResponseEntity.ok(mediaAssetService.completeUpload(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaAsset> getAsset(@PathVariable String id) {
        return ResponseEntity.ok(mediaAssetService.getById(id));
    }

    @GetMapping("/{id}/playback")
    public ResponseEntity<Map<String, String>> getPlaybackUrl(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        String url = mediaAssetService.getPlaybackUrl(id, principal.userId());
        return ResponseEntity.ok(Map.of("url", url));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        mediaAssetService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
