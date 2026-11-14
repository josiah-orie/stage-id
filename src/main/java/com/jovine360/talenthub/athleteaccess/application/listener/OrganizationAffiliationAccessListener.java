package com.jovine360.talenthub.athleteaccess.application.listener;

import com.jovine360.talenthub.athleteaccess.application.service.AthleteAccessService;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.athleteaccess.domain.model.GranteeType;
import com.jovine360.talenthub.organization.api.OrganizationAthleteAffiliatedEvent;
import com.jovine360.talenthub.organization.api.OrganizationAthleteAffiliationEndedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * An approved affiliation grants the organization access to the athlete. Staff of that
 * organization (OWNER, ADMIN, COACH, ATHLETE_MANAGER) then inherit it through the policy.
 * Synchronous so the grant commits with the approval.
 */
@Component
public class OrganizationAffiliationAccessListener {

    private final AthleteAccessService accessService;

    public OrganizationAffiliationAccessListener(AthleteAccessService accessService) {
        this.accessService = accessService;
    }

    @EventListener
    public void onAffiliated(OrganizationAthleteAffiliatedEvent event) {
        accessService.grantAccess(event.athleteUserId(), event.organizationId(), GranteeType.ORGANIZATION,
                AthletePermission.valueOf(event.permission()), null);
    }

    @EventListener
    public void onEnded(OrganizationAthleteAffiliationEndedEvent event) {
        accessService.revokeAccess(event.athleteUserId(), event.organizationId(), GranteeType.ORGANIZATION);
    }
}
