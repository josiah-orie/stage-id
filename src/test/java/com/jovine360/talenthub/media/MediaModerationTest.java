package com.jovine360.talenthub.media;

import com.jovine360.talenthub.athleteaccess.api.AthleteAccessPolicy;
import com.jovine360.talenthub.media.application.listener.MediaReportedListener;
import com.jovine360.talenthub.media.application.service.MediaAssetService;
import com.jovine360.talenthub.media.application.service.MediaStorageService;
import com.jovine360.talenthub.media.domain.model.*;
import com.jovine360.talenthub.media.domain.repository.MediaAccessAuditRepository;
import com.jovine360.talenthub.media.domain.repository.MediaAssetRepository;
import com.jovine360.talenthub.media.domain.repository.MediaUploadSessionRepository;
import com.jovine360.talenthub.safeguarding.api.SafeguardingReportSubmittedEvent;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Report → flag → restrict → playback denied. No Spring context. */
class MediaModerationTest {

    private static final String ATHLETE = "athlete-1";

    private MediaAsset asset;
    private MediaStorageService storage;
    private MediaAssetService service;

    @BeforeEach
    void setUp() {
        MediaAssetRepository repo = mock(MediaAssetRepository.class);
        storage = mock(MediaStorageService.class);
        AthleteAccessPolicy policy = mock(AthleteAccessPolicy.class);
        when(policy.canAccess(eq(ATHLETE), eq(ATHLETE), any(), any())).thenReturn(true);
        asset = new MediaAsset();
        asset.setId("asset-1");
        asset.setOwnerId(ATHLETE);
        asset.setOwnerType(OwnerType.ATHLETE);
        asset.setAssetType(AssetType.ACTIVITY_VIDEO);
        asset.setUploadedBy(ATHLETE);
        asset.setProcessingStatus(ProcessingStatus.READY);
        asset.setBucketName("b");
        asset.setObjectKey("k");
        when(repo.findById("asset-1")).thenReturn(Optional.of(asset));
        when(repo.save(any(MediaAsset.class))).thenAnswer(i -> i.getArgument(0));
        when(storage.generatePresignedDownloadUrl("b", "k")).thenReturn("https://signed");
        service = new MediaAssetService(repo, mock(MediaUploadSessionRepository.class),
                mock(MediaAccessAuditRepository.class), storage, mock(ApplicationEventPublisher.class), policy);
    }

    @Test
    void reportFlagsThenRestrictBlocksScoutPlaybackButNotOwner() {
        new MediaReportedListener(service).onReport(
                new SafeguardingReportSubmittedEvent("r1", "asset-1", "MEDIA", "INAPPROPRIATE_CONTENT"));
        assertThat(asset.getModerationStatus()).isEqualTo(ModerationStatus.FLAGGED);

        // still visible while flagged
        assertThat(service.getPlaybackUrl("asset-1", "scout-1", List.of("SCOUT"))).isEqualTo("https://signed");

        service.moderate("asset-1", ModerationStatus.RESTRICTED, "breach", "admin-1");
        assertThatThrownBy(() -> service.getPlaybackUrl("asset-1", "scout-1", List.of("SCOUT")))
                .isInstanceOf(SecurityException.class).hasMessageContaining("not accessible");
        assertThat(service.getPlaybackUrl("asset-1", ATHLETE, List.of("ATHLETE"))).isEqualTo("https://signed");
    }

    @Test
    void reportsOnOtherEntitiesAreIgnored() {
        new MediaReportedListener(service).onReport(
                new SafeguardingReportSubmittedEvent("r1", "asset-1", "USER", "HARASSMENT"));
        assertThat(asset.getModerationStatus()).isEqualTo(ModerationStatus.CLEAN);
    }

    @Test
    void sameDecisionTwiceIsConflict() {
        assertThatThrownBy(() -> service.moderate("asset-1", ModerationStatus.CLEAN, null, "admin-1"))
                .isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(() -> service.moderate("asset-1", ModerationStatus.FLAGGED, null, "admin-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
