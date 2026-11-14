package com.jovine360.talenthub.media.application.listener;

import com.jovine360.talenthub.media.application.service.MediaAssetService;
import com.jovine360.talenthub.safeguarding.api.SafeguardingReportSubmittedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** A safeguarding report against a media asset flags it for admin review. */
@Component
public class MediaReportedListener {

    private final MediaAssetService mediaAssetService;

    public MediaReportedListener(MediaAssetService mediaAssetService) {
        this.mediaAssetService = mediaAssetService;
    }

    @EventListener
    public void onReport(SafeguardingReportSubmittedEvent event) {
        if (event.reportedEntityType() != null
                && ("MEDIA".equalsIgnoreCase(event.reportedEntityType())
                    || "MEDIA_ASSET".equalsIgnoreCase(event.reportedEntityType()))) {
            mediaAssetService.flag(event.reportedEntityId(), "Reported: " + event.reportType());
        }
    }
}
