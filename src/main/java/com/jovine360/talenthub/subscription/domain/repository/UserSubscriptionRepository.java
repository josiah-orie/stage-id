package com.jovine360.talenthub.subscription.domain.repository;

import com.jovine360.talenthub.subscription.domain.model.SubscriptionStatus;
import com.jovine360.talenthub.subscription.domain.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, String> {
    List<UserSubscription> findBySubscriberIdAndStatus(String subscriberId, SubscriptionStatus status);
    Optional<UserSubscription> findFirstBySubscriberIdAndStatusOrderByStartsAtDesc(
            String subscriberId, SubscriptionStatus status);
}
