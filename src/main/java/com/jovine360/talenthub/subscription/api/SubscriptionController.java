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

    public SubscriptionController(SubscriptionLifecycleService lifecycleService,
                                   UserSubscriptionRepository subscriptionRepository,
                                   SubscriptionPlanRepository planRepository) {
        this.lifecycleService = lifecycleService;
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
    }

    @GetMapping("/subscription-plans")
    public ResponseEntity<List<SubscriptionPlan>> listPlans() {
        return ResponseEntity.ok(planRepository.findByActiveTrue());
    }

    record CreateSubscriptionRequest(String planId) {}

    @PostMapping("/subscriptions")
    public ResponseEntity<UserSubscription> createSubscription(
            @RequestBody CreateSubscriptionRequest req,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        SubscriptionPlan plan = planRepository.findById(req.planId())
                .orElseThrow(() -> new EntityNotFoundException("SubscriptionPlan", req.planId()));
        UserSubscription sub = lifecycleService.createSubscription(principal.userId(), plan);
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
        return ResponseEntity.ok(lifecycleService.cancelSubscription(
                id, principal.userId(), reason, immediate));
    }

    @GetMapping("/subscriptions/{id}/addons")
    public ResponseEntity<Void> getAddons(@PathVariable String id) {
        return ResponseEntity.ok().build();
    }
}
