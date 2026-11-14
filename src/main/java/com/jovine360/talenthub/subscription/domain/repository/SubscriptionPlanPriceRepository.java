package com.jovine360.talenthub.subscription.domain.repository;

import com.jovine360.talenthub.subscription.domain.model.SubscriptionPlanPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionPlanPriceRepository extends JpaRepository<SubscriptionPlanPrice, String> {
    List<SubscriptionPlanPrice> findByPlanId(String planId);
}
