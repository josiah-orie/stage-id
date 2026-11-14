package com.jovine360.talenthub.media;

import com.jovine360.talenthub.athleteaccess.api.AthleteAccessPolicy;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.media.application.service.MediaAssetService;
import com.jovine360.talenthub.media.application.service.MediaStorageService;
import com.jovine360.talenthub.media.domain.model.AssetType;
import com.jovine360.talenthub.media.domain.model.MediaAsset;
import com.jovine360.talenthub.media.domain.model.OwnerType;
import com.jovine360.talenthub.media.domain.repository.MediaAccessAuditRepository;
import com.jovine360.talenthub.media.domain.repository.MediaAssetRepository;
import com.jovine360.talenthub.media.domain.repository.MediaUploadSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Ownership checks on media upload, view and delete. No Spring context. */
class MediaAccessTest {

    private static final String ATHLETE = "athlete-1";

    private MediaAssetRepository assets;
    private MediaStorageService storage;
    private AthleteAccessPolicy policy;
    private MediaAssetService service;

    @BeforeEach
    void setUp() {
        assets = mock(MediaAssetRepository.class);
        storage = mock(MediaStorageService.class);
        policy = mock(AthleteAccessPolicy.class);
        when(assets.save(any(MediaAsset.class))).thenAnswer(i -> {
            MediaAsset a = i.getArgument(0);
            if (a.getId() == null) a.setId("asset-1");
            return a;
        });
        when(policy.canAccess(eq(ATHLETE), eq(ATHLETE), any(), any())).thenReturn(true);
        service = new MediaAssetService(assets, mock(MediaUploadSessionRepository.class),
                mock(MediaAccessAuditRepository.class), storage, mock(ApplicationEventPublisher.class), policy);
    }

    private MediaAsset video() {
        MediaAsset a = new MediaAsset();
        a.setId("asset-1");
        a.setOwnerId(ATHLETE);
        a.setOwnerType(OwnerType.ATHLETE);
        a.setAssetType(AssetType.ACTIVITY_VIDEO);
        a.setUploadedBy(ATHLETE);
        a.setBucketName("athlete-media");
        a.setObjectKey("k");
        when(assets.findById("asset-1")).thenReturn(Optional.of(a));
        return a;
    }

    @Test
    void ownerCanUpload() {
        var res = service.initiateUpload(ATHLETE, OwnerType.ATHLETE, AssetType.ACTIVITY_VIDEO,
                "clip.mp4", "video/mp4", ATHLETE, List.of("ATHLETE"));
        assertThat(res.assetId()).isEqualTo("asset-1");
    }

    @Test
    void unrelatedUserCannotUploadForAthlete() {
        assertThatThrownBy(() -> service.initiateUpload(ATHLETE, OwnerType.ATHLETE, AssetType.ACTIVITY_VIDEO,
                "clip.mp4", "video/mp4", "stranger", List.of("COACH")))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void ownerCanDeleteButStrangerCannot() {
        video();
        assertThatThrownBy(() -> service.softDelete("asset-1", "stranger", List.of("ATHLETE")))
                .isInstanceOf(SecurityException.class);
        verify(storage, never()).softDelete(any(), any());

        service.softDelete("asset-1", ATHLETE, List.of("ATHLETE"));
        verify(storage).softDelete("athlete-media", "k");
    }

    @Test
    void scoutCanViewVideoButNotVerificationDocument() {
        MediaAsset a = video();
        assertThat(service.getById("asset-1", "scout-1", List.of("SCOUT"))).isSameAs(a);

        a.setAssetType(AssetType.VERIFICATION_DOCUMENT);
        assertThatThrownBy(() -> service.getById("asset-1", "scout-1", List.of("SCOUT")))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void grantHolderCanView() {
        video().setAssetType(AssetType.VERIFICATION_DOCUMENT);
        when(policy.canAccess(ATHLETE, "agent-1", List.of("AGENT"), AthletePermission.VIEW_STATS)).thenReturn(true);
        assertThat(service.getById("asset-1", "agent-1", List.of("AGENT"))).isNotNull();
    }

    @Test
    void adminCanDeleteAnything() {
        video();
        service.softDelete("asset-1", "admin-1", List.of("ADMIN"));
        verify(storage).softDelete("athlete-media", "k");
    }
}
