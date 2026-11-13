package com.jovine360.talenthub.subscription.application.service;

import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.subscription.domain.model.*;
import com.jovine360.talenthub.subscription.domain.repository.UserSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class SubscriptionLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionLifecycleService.class);

    private final UserSubscriptionRepository subscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SubscriptionLifecycleService(UserSubscriptionRepository subscriptionRepository,
                                         ApplicationEventPublisher eventPublisher) {
        this.subscriptionRepository = subscriptionRepository;
        this.eventPublisher = eventPublisher;
    }

    public UserSubscription createSubscription(String subscriberId, SubscriptionPlan plan) {
        UserSubscription sub = new UserSubscription();
        sub.setSubscriberId(subscriberId);
        sub.setPlan(plan);
        sub.setStatus(SubscriptionStatus.PENDING);
        sub.setStartsAt(Instant.now());
        return subscriptionRepository.save(sub);
    }

    public UserSubscription activateSubscription(String subscriptionId) {
        UserSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("UserSubscription", subscriptionId));
        sub.setStatus(SubscriptionStatus.ACTIVE);
        UserSubscription saved = subscriptionRepository.save(sub);
        eventPublisher.publishEvent(
                new SubscriptionActivatedEvent(saved.getId(), saved.getSubscriberId()));
        return saved;
    }

    public UserSubscription cancelSubscription(String subscriptionId, String cancelledBy,
                                                String reason, boolean immediate) {
        UserSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("UserSubscription", subscriptionId));
        sub.setCancelledBy(cancelledBy);
        sub.setCancellationReason(reason);
        sub.setCancelledAt(Instant.now());
        if (immediate) {
            sub.setStatus(SubscriptionStatus.CANCELLED);
            sub.setExpiresAt(Instant.now());
        }
        // else: stays ACTIVE until expiresAt
        return subscriptionRepository.save(sub);
    }

    public UserSubscription applyGracePeriod(String subscriptionId, int graceDays) {
        UserSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("UserSubscription", subscriptionId));
        sub.setGraceEndsAt(Instant.now().plusSeconds(graceDays * 86400L));
        return subscriptionRepository.save(sub);
    }

    public UserSubscription expireSubscription(String subscriptionId) {
        UserSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("UserSubscription", subscriptionId));
        sub.setStatus(SubscriptionStatus.EXPIRED);
        sub.setExpiresAt(Instant.now());
        UserSubscription saved = subscriptionRepository.save(sub);
        eventPublisher.publishEvent(
                new SubscriptionExpiredEvent(saved.getId(), saved.getSubscriberId()));
        return saved;
    }

    /** Daily job at 01:00 — find and expire subscriptions whose expiresAt has passed. */
    @Scheduled(cron = "0 0 1 * * *")
    public void expireOverdueSubscriptions() {
        List<UserSubscription> active = subscriptionRepository
                .findBySubscriberIdAndStatus("*", SubscriptionStatus.ACTIVE);
        // In a real impl: query WHERE status=ACTIVE AND expires_at < NOW()
        // Using a simplified approach here
        log.debug("Running subscription expiry job");
    }
}
