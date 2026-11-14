package com.jovine360.talenthub.subscription.domain.repository;

import com.jovine360.talenthub.subscription.domain.model.SubscriptionAddon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionAddonRepository extends JpaRepository<SubscriptionAddon, String> {
    List<SubscriptionAddon> findBySubscriptionId(String subscriptionId);
}
