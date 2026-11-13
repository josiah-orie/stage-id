package com.jovine360.talenthub.subscription.domain.repository;

import com.jovine360.talenthub.subscription.domain.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
    Optional<SubscriptionPlan> findByCode(String code);
    java.util.List<SubscriptionPlan> findByActiveTrue();
}
