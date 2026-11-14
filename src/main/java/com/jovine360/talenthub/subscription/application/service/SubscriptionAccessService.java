package com.jovine360.talenthub.subscription.application.service;

import com.jovine360.talenthub.organization.api.OrganizationApi;
import com.jovine360.talenthub.scout.api.ScoutApi;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.subscription.api.SubscriptionApi;
import com.jovine360.talenthub.subscription.domain.model.SubscriptionStatus;
import com.jovine360.talenthub.subscription.domain.model.UserSubscription;
import com.jovine360.talenthub.subscription.domain.repository.UserSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@Transactional
public class SubscriptionAccessService implements SubscriptionApi {

    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionLifecycleService lifecycleService;
    private final OrganizationApi organizationApi;
    private final ScoutApi scoutApi;

    public SubscriptionAccessService(UserSubscriptionRepository subscriptionRepository,
                                     SubscriptionLifecycleService lifecycleService,
                                     OrganizationApi organizationApi, ScoutApi scoutApi) {
        this.subscriptionRepository = subscriptionRepository;
        this.lifecycleService = lifecycleService;
        this.organizationApi = organizationApi;
        this.scoutApi = scoutApi;
    }

    /** The entity, after the ownership check. */
    @Transactional(readOnly = true)
    public UserSubscription loadOwnedEntity(String subscriptionId, String userId, Collection<String> roles) {
        UserSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("UserSubscription", subscriptionId));
        String owner = sub.getSubscriberId();
        boolean allowed = owner.equals(userId)
                || roles.contains("ADMIN") || roles.contains("SUPER_ADMIN")
                || scoutApi.findScoutProfileIdByUserId(userId).map(owner::equals).orElse(false)
                || organizationApi.isOrganizationAdmin(owner, userId);
        if (!allowed) {
            throw new SecurityException("This subscription belongs to someone else");
        }
        return sub;
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionSummary loadOwned(String subscriptionId, String userId, Collection<String> roles) {
        UserSubscription s = loadOwnedEntity(subscriptionId, userId, roles);
        return new SubscriptionSummary(s.getId(), s.getSubscriberId(), s.getStatus().name(),
                s.getPlan().getCode(), s.getPlan().getPricePerMonth(), s.getPlan().getCurrency());
    }

    @Override
    public void activate(String subscriptionId) {
        UserSubscription s = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("UserSubscription", subscriptionId));
        if (s.getStatus() != SubscriptionStatus.ACTIVE) {
            lifecycleService.activateSubscription(subscriptionId);
        }
    }
}
