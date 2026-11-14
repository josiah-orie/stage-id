package com.jovine360.talenthub.athleteaccess.application.listener;

import com.jovine360.talenthub.athleteaccess.application.service.AthleteAccessService;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.athleteaccess.domain.model.GranteeType;
import com.jovine360.talenthub.guardian.api.GuardianLinkActivatedEvent;
import com.jovine360.talenthub.guardian.api.GuardianLinkEndedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Keeps access grants in step with agent relationships. Runs synchronously inside the
 * publisher's transaction so a grant exists as soon as the approval commits.
 * PARENT and LEGAL_GUARDIAN links are checked directly by the access policy and need no grant.
 */
@Component
public class GuardianLinkAccessListener {

    private static final String AGENT = "AGENT";

    private final AthleteAccessService accessService;

    public GuardianLinkAccessListener(AthleteAccessService accessService) {
        this.accessService = accessService;
    }

    @EventListener
    public void onActivated(GuardianLinkActivatedEvent event) {
        if (!AGENT.equals(event.relationshipType())) return;
        AthletePermission permission = event.permission() != null
                ? AthletePermission.valueOf(event.permission()) : AthletePermission.VIEW_STATS;
        accessService.grantAccess(event.athleteUserId(), event.guardianUserId(),
                GranteeType.USER, permission, event.expiresAt());
    }

    @EventListener
    public void onEnded(GuardianLinkEndedEvent event) {
        if (!AGENT.equals(event.relationshipType())) return;
        accessService.revokeAccess(event.athleteUserId(), event.guardianUserId(), GranteeType.USER);
    }
}
