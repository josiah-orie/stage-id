package com.jovine360.talenthub.athleteaccess;

import com.jovine360.talenthub.activity.application.service.ActivityQueryService;
import com.jovine360.talenthub.activity.application.service.ActivitySubmissionService;
import com.jovine360.talenthub.activity.domain.model.ActivityStatus;
import com.jovine360.talenthub.activity.domain.model.AthleteActivity;
import com.jovine360.talenthub.activity.domain.model.VisibilityLevel;
import com.jovine360.talenthub.activity.infrastructure.web.AthleteActivityController;
import com.jovine360.talenthub.athlete.api.AthleteController;
import com.jovine360.talenthub.athlete.api.AthleteProfileDto;
import com.jovine360.talenthub.athlete.application.service.AthleteService;
import com.jovine360.talenthub.athleteaccess.application.service.AthleteAccessService;
import com.jovine360.talenthub.athleteaccess.domain.repository.AthleteAccessGrantRepository;
import com.jovine360.talenthub.development.application.service.DevelopmentTimelineService;
import com.jovine360.talenthub.development.application.service.PerformanceMeasurementService;
import com.jovine360.talenthub.development.infrastructure.web.DevelopmentController;
import com.jovine360.talenthub.guardian.api.GuardianApi;
import com.jovine360.talenthub.media.application.service.MediaAssetService;
import com.jovine360.talenthub.media.application.service.MediaStorageService;
import com.jovine360.talenthub.media.domain.model.AssetType;
import com.jovine360.talenthub.media.domain.model.MediaAsset;
import com.jovine360.talenthub.media.domain.model.OwnerType;
import com.jovine360.talenthub.media.domain.repository.MediaAccessAuditRepository;
import com.jovine360.talenthub.media.domain.repository.MediaAssetRepository;
import com.jovine360.talenthub.media.domain.repository.MediaUploadSessionRepository;
import com.jovine360.talenthub.safeguarding.application.service.ConsentService;
import com.jovine360.talenthub.safeguarding.application.service.SafeguardingReportService;
import com.jovine360.talenthub.safeguarding.domain.model.ConsentPurpose;
import com.jovine360.talenthub.safeguarding.infrastructure.web.SafeguardingController;
import com.jovine360.talenthub.shared.api.PageResponse;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.exception.SafeguardingViolationException;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Owner gets 2xx, unrelated user gets 403, for every athlete-data controller.
 * Uses the real access policy with mocked repositories. No Spring context.
 */
class AthleteDataEndpointsAuthorizationTest {

    private static final String ATHLETE = "athlete-1";
    private static final AuthenticatedPrincipal OWNER = AuthenticatedPrincipal.of(ATHLETE, "a@x.io", List.of("ATHLETE"));
    private static final AuthenticatedPrincipal STRANGER = AuthenticatedPrincipal.of("user-9", "s@x.io", List.of("ATHLETE"));
    private static final AuthenticatedPrincipal SCOUT = AuthenticatedPrincipal.of("scout-1", "sc@x.io", List.of("SCOUT"));
    private static final AuthenticatedPrincipal GUARDIAN = AuthenticatedPrincipal.of("parent-1", "p@x.io", List.of("PARENT"));

    private AthleteAccessService policy;
    private GuardianApi guardianApi;

    @BeforeEach
    void setUp() {
        AthleteAccessGrantRepository grants = mock(AthleteAccessGrantRepository.class);
        when(grants.findByAthleteUserIdAndGranteeIdAndGranteeTypeAndActiveTrue(any(), any(), any())).thenReturn(List.of());
        guardianApi = mock(GuardianApi.class);
        when(guardianApi.hasActiveGuardianLink("parent-1", ATHLETE)).thenReturn(true);
        policy = new AthleteAccessService(grants, guardianApi,
                mock(com.jovine360.talenthub.organization.api.OrganizationApi.class));
    }

    // ---- activities --------------------------------------------------------------------------

    @Test
    void activitiesOwnerAndGuardianSeeAllStrangerDeniedScoutSeesPublicOnly() {
        ActivityQueryService query = mock(ActivityQueryService.class);
        when(query.getAthleteActivities(eq(ATHLETE), any(), anyBoolean())).thenReturn(PageResponse.empty(0, 20));
        var controller = new AthleteActivityController(mock(ActivitySubmissionService.class), query, policy);
        var page = PageRequest.of(0, 20);

        assertThat(controller.listActivities(ATHLETE, page, OWNER).getStatusCode().value()).isEqualTo(200);
        verify(query).getAthleteActivities(ATHLETE, page, false);

        controller.listActivities(ATHLETE, page, GUARDIAN);

        controller.listActivities(ATHLETE, page, SCOUT);
        verify(query).getAthleteActivities(ATHLETE, page, true);

        assertThatThrownBy(() -> controller.listActivities(ATHLETE, page, STRANGER))
                .isInstanceOf(SafeguardingViolationException.class);
    }

    @Test
    void scoutCannotOpenPrivateActivity() {
        AthleteActivity privateDraft = activity(ActivityStatus.DRAFT, VisibilityLevel.PRIVATE);
        AthleteActivity publicPublished = activity(ActivityStatus.PUBLISHED, VisibilityLevel.PUBLIC);
        ActivityQueryService query = mock(ActivityQueryService.class);
        when(query.getById("act-private")).thenReturn(privateDraft);
        when(query.getById("act-public")).thenReturn(publicPublished);
        var controller = new AthleteActivityController(mock(ActivitySubmissionService.class), query, policy);

        assertThat(controller.getActivity("act-private", OWNER).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.getActivity("act-public", SCOUT).getStatusCode().value()).isEqualTo(200);
        assertThatThrownBy(() -> controller.getActivity("act-private", SCOUT))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> controller.getActivity("act-public", STRANGER))
                .isInstanceOf(SafeguardingViolationException.class);
    }

    // ---- development ---------------------------------------------------------------------------

    @Test
    void developmentOwnerAllowedStrangerDenied() {
        var controller = new DevelopmentController(mock(DevelopmentTimelineService.class),
                mock(PerformanceMeasurementService.class), policy);

        assertThat(controller.getDevelopmentTimeline(ATHLETE, OWNER).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.getPerformanceChart(ATHLETE, null, null, GUARDIAN).getStatusCode().value()).isEqualTo(200);
        assertThatThrownBy(() -> controller.getDevelopmentTimeline(ATHLETE, STRANGER))
                .isInstanceOf(SafeguardingViolationException.class);
        assertThatThrownBy(() -> controller.getPerformanceChart(ATHLETE, null, "SPRINT_100M", SCOUT))
                .isInstanceOf(SafeguardingViolationException.class);
    }

    // ---- athlete profile -----------------------------------------------------------------------

    @Test
    void athleteProfileByUserIdDeniedToStrangerAndNumberLookupIsPublicProjection() {
        AthleteService athletes = mock(AthleteService.class);
        AthleteProfileDto full = new AthleteProfileDto("p1", ATHLETE, "TH-00001", "sport-1", "FOOTBALL",
                "Midfielder", 175, 70, "RIGHT", "ACTIVE", "org-1", "NG");
        when(athletes.getByUserId(ATHLETE)).thenReturn(full);
        when(athletes.findByAthleteNumber("TH-00001")).thenReturn(Optional.of(full));
        var controller = new AthleteController(athletes, policy);

        assertThat(controller.getByUserId(ATHLETE, OWNER).getBody()).isEqualTo(full);
        assertThatThrownBy(() -> controller.getByUserId(ATHLETE, STRANGER))
                .isInstanceOf(SafeguardingViolationException.class);

        AthleteProfileDto projected = controller.getByAthleteNumber("TH-00001", STRANGER).getBody();
        assertThat(projected.athleteNumber()).isEqualTo("TH-00001");
        assertThat(projected.heightCm()).isNull();
        assertThat(projected.organizationId()).isNull();
        assertThat(controller.getByAthleteNumber("TH-00001", OWNER).getBody()).isEqualTo(full);
    }

    // ---- media ---------------------------------------------------------------------------------

    @Test
    void mediaDeleteAndUploadOnlyForOwnerOrManager() {
        MediaAssetRepository assets = mock(MediaAssetRepository.class);
        MediaAsset asset = new MediaAsset();
        asset.setId("m1");
        asset.setOwnerId(ATHLETE);
        asset.setOwnerType(OwnerType.ATHLETE);
        asset.setAssetType(AssetType.VERIFICATION_DOCUMENT);
        asset.setUploadedBy(ATHLETE);
        asset.setBucketName("b");
        asset.setObjectKey("k");
        when(assets.findById("m1")).thenReturn(Optional.of(asset));
        when(assets.save(any(MediaAsset.class))).thenAnswer(i -> i.getArgument(0));
        var service = new MediaAssetService(assets, mock(MediaUploadSessionRepository.class),
                mock(MediaAccessAuditRepository.class), mock(MediaStorageService.class),
                mock(ApplicationEventPublisher.class), policy);

        assertThatThrownBy(() -> service.softDelete("m1", STRANGER.userId(), STRANGER.roles()))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> service.getById("m1", SCOUT.userId(), SCOUT.roles()))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> service.initiateUpload(ATHLETE, OwnerType.ATHLETE, AssetType.ACTIVITY_VIDEO,
                "clip.mp4", "video/mp4", STRANGER.userId(), STRANGER.roles()))
                .isInstanceOf(SecurityException.class);

        assertThat(service.getById("m1", GUARDIAN.userId(), GUARDIAN.roles())).isSameAs(asset);
        service.softDelete("m1", OWNER.userId(), OWNER.roles());
        assertThat(asset.getDeletedAt()).isNotNull();
    }

    // ---- consent -------------------------------------------------------------------------------

    @Test
    void consentOwnerAllowedStrangerDeniedAndMinorRuleApplied() {
        ConsentService consent = mock(ConsentService.class);
        var controller = new SafeguardingController(consent, mock(SafeguardingReportService.class), policy, guardianApi,
                mock(com.jovine360.talenthub.shared.domain.service.AuditTrail.class),
                mock(com.jovine360.talenthub.safeguarding.application.service.DataSubjectRequestService.class));

        assertThatThrownBy(() -> controller.revokeConsent(ATHLETE, ConsentPurpose.MEDIA_SHARING, STRANGER))
                .isInstanceOf(SafeguardingViolationException.class);
        assertThat(controller.revokeConsent(ATHLETE, ConsentPurpose.MEDIA_SHARING, OWNER).getStatusCode().value())
                .isEqualTo(204);
    }

    private static AthleteActivity activity(ActivityStatus status, VisibilityLevel visibility) {
        AthleteActivity a = new AthleteActivity();
        a.setAthleteId(ATHLETE);
        a.setActivityStatus(status);
        a.setVisibilityLevel(visibility);
        return a;
    }
}
