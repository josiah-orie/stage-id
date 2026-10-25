package com.jovine360.talenthub.subscription.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jovine360.talenthub.shared.domain.exception.EntitlementLimitExceededException;
import com.jovine360.talenthub.shared.domain.service.EntitlementService;
import com.jovine360.talenthub.subscription.domain.model.SubscriptionStatus;
import com.jovine360.talenthub.subscription.domain.model.UserSubscription;
import com.jovine360.talenthub.subscription.domain.repository.UserSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Phase 1 stub implementation of {@link EntitlementService}.
 * Returns free-tier limits for all subscribers; full enforcement added in Phase 4.
 */
@Service
@Transactional(readOnly = true)
public class DefaultEntitlementService implements EntitlementService {

    private static final Logger log = LoggerFactory.getLogger(DefaultEntitlementService.class);

    /** Free-tier defaults returned when no active subscription is found. */
    private static final Map<String, Long> FREE_TIER_LIMITS = Map.of(
            "max_athletes",         5L,
            "max_scouts",           0L,
            "max_video_uploads",    3L,
            "max_org_members",      10L,
            "video_max_duration_s", 120L
    );

    private final UserSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    public DefaultEntitlementService(UserSubscriptionRepository subscriptionRepository,
                                     ObjectMapper objectMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public long getLimit(String subscriberId, String key) {
        UserSubscription sub = subscriptionRepository
                .findFirstBySubscriberIdAndStatusOrderByStartsAtDesc(subscriberId, SubscriptionStatus.ACTIVE)
                .filter(UserSubscription::isActive)
                .orElse(null);

        if (sub != null && sub.getPlan().getEntitlementsJson() != null) {
            try {
                Map<String, Long> entitlements = objectMapper.readValue(
                        sub.getPlan().getEntitlementsJson(),
                        new TypeReference<>() {});
                return entitlements.getOrDefault(key, Long.MAX_VALUE);
            } catch (Exception e) {
                log.warn("Could not parse entitlements JSON for subscriber {}: {}", subscriberId, e.getMessage());
            }
        }

        // Fall back to free-tier defaults
        return FREE_TIER_LIMITS.getOrDefault(key, Long.MAX_VALUE);
    }

    @Override
    public boolean isWithinLimit(String subscriberId, String key, long current) {
        return current < getLimit(subscriberId, key);
    }

    @Override
    public void assertWithinLimit(String subscriberId, String key, long current) {
        long limit = getLimit(subscriberId, key);
        if (current >= limit) {
            throw new EntitlementLimitExceededException(key, limit, current);
        }
    }

    @Override
    public boolean hasActivePlan(String subscriberId) {
        return subscriptionRepository
                .findFirstBySubscriberIdAndStatusOrderByStartsAtDesc(subscriberId, SubscriptionStatus.ACTIVE)
                .filter(UserSubscription::isActive)
                .isPresent();
    }
}
