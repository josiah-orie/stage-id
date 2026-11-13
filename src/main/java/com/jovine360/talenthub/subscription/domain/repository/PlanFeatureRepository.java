package com.jovine360.talenthub.subscription.domain.repository;

import com.jovine360.talenthub.subscription.domain.model.PlanFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanFeatureRepository extends JpaRepository<PlanFeature, String> {
    List<PlanFeature> findByPlanId(String planId);
    Optional<PlanFeature> findByPlanIdAndEntitlementKey(String planId, String entitlementKey);
}
