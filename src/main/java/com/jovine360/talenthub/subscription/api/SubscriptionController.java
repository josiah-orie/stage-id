package com.jovine360.talenthub.subscription.api;

import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import com.jovine360.talenthub.subscription.application.service.SubscriptionLifecycleService;
import com.jovine360.talenthub.subscription.domain.model.SubscriptionPlan;
import com.jovine360.talenthub.subscription.domain.model.UserSubscription;
import com.jovine360.talenthub.subscription.domain.repository.UserSubscriptionRepository;
import com.jovine360.talenthub.subscription.domain.repository.SubscriptionPlanRepository;
import com.jovine360.talenthub.subscription.domain.model.SubscriptionStatus;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SubscriptionController {

    private final SubscriptionLifecycleService lifecycleService;
    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final com.jovine360.talenthub.subscription.domain.repository.SubscriptionAddonRepository addonRepository;
    private final com.jovine360.talenthub.subscription.application.service.SubscriptionAccessService accessService;

    public SubscriptionController(SubscriptionLifecycleService lifecycleService,
                                   UserSubscriptionRepository subscriptionRepository,
                                   SubscriptionPlanRepository planRepository,
                                   com.jovine360.talenthub.subscription.domain.repository.SubscriptionAddonRepository addonRepository,
                                   com.jovine360.talenthub.subscription.application.service.SubscriptionAccessService accessService) {
        this.lifecycleService = lifecycleService;
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.addonRepository = addonRepository;
        this.accessService = accessService;
    }

    private UserSubscription loadOwned(String subscriptionId, AuthenticatedPrincipal p) {
        return accessService.loadOwnedEntity(subscriptionId, p.userId(), p.roles());
    }

    @GetMapping("/subscription-plans")
    public ResponseEntity<List<SubscriptionPlan>> listPlans() {
        return ResponseEntity.ok(planRepository.findByActiveTrue());
    }

    record CreateSubscriptionRequest(@jakarta.validation.constraints.NotBlank String planId) {}

    @PostMapping("/subscriptions")
    public ResponseEntity<UserSubscription> createSubscription(
            @jakarta.validation.Valid @RequestBody CreateSubscriptionRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        SubscriptionPlan plan = planRepository.findById(req.planId())
                .orElseThrow(() -> new EntityNotFoundException("SubscriptionPlan", req.planId()));
        if (!plan.isActive()) {
            throw new IllegalArgumentException("This plan is no longer available");
        }
        UserSubscription sub = lifecycleService.createSubscription(principal.userId(), plan);
        // Free plans need no payment and start straight away; paid plans stay PENDING until checkout succeeds
        if (plan.getPricePerMonth() == null || plan.getPricePerMonth().signum() == 0) {
            sub = lifecycleService.activateSubscription(sub.getId());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(sub);
    }

    @GetMapping("/subscriptions/me")
    public ResponseEntity<UserSubscription> getMySubscription(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return subscriptionRepository
                .findFirstBySubscriberIdAndStatusOrderByStartsAtDesc(
                        principal.userId(), SubscriptionStatus.ACTIVE)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/subscriptions/{id}/cancel")
    public ResponseEntity<UserSubscription> cancelSubscription(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "false") boolean immediate,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        loadOwned(id, principal);
        return ResponseEntity.ok(lifecycleService.cancelSubscription(
                id, principal.userId(), reason, immediate));
    }

    @GetMapping("/subscriptions/{id}/addons")
    public ResponseEntity<List<com.jovine360.talenthub.subscription.domain.model.SubscriptionAddon>> getAddons(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        loadOwned(id, principal);
        return ResponseEntity.ok(addonRepository.findBySubscriptionId(id));
    }
}
