package com.jovine360.talenthub.subscription.application.service;

import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.subscription.api.AdminPlanDto;
import com.jovine360.talenthub.subscription.api.PlanRequests.CreatePlanRequest;
import com.jovine360.talenthub.subscription.api.PlanRequests.PriceRequest;
import com.jovine360.talenthub.subscription.api.PlanRequests.UpdatePlanRequest;
import com.jovine360.talenthub.subscription.domain.model.PlanFeature;
import com.jovine360.talenthub.subscription.domain.model.SubscriptionPlan;
import com.jovine360.talenthub.subscription.domain.model.SubscriptionPlanPrice;
import com.jovine360.talenthub.subscription.domain.repository.PlanFeatureRepository;
import com.jovine360.talenthub.subscription.domain.repository.SubscriptionPlanPriceRepository;
import com.jovine360.talenthub.subscription.domain.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SUPER_ADMIN management of subscription plans. Entitlement limits are stored both as the
 * plan's {@code entitlements_json} (read by the entitlement service) and as {@code plan_features} rows.
 */
@Service
@Transactional
public class PlanAdministrationService {

    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionPlanPriceRepository priceRepository;
    private final PlanFeatureRepository featureRepository;
    private final JsonMapper jsonMapper;

    public PlanAdministrationService(SubscriptionPlanRepository planRepository,
                                     SubscriptionPlanPriceRepository priceRepository,
                                     PlanFeatureRepository featureRepository,
                                     JsonMapper jsonMapper) {
        this.planRepository = planRepository;
        this.priceRepository = priceRepository;
        this.featureRepository = featureRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional(readOnly = true)
    public List<AdminPlanDto> listAll() {
        return planRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AdminPlanDto get(String id) {
        return toDto(load(id));
    }

    public AdminPlanDto create(CreatePlanRequest req) {
        if (planRepository.findByCode(req.code()).isPresent()) {
            throw new DuplicateResourceException("SubscriptionPlan", "code", req.code());
        }
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setCode(req.code());
        plan.setName(req.name());
        plan.setTargetRole(req.targetRole());
        plan.setPricePerMonth(req.pricePerMonth());
        if (req.currency() != null) plan.setCurrency(req.currency().toUpperCase());
        plan.setActive(true);
        plan.setEntitlementsJson(writeEntitlements(req.entitlements()));
        SubscriptionPlan saved = planRepository.save(plan);
        replaceFeatures(saved.getId(), req.entitlements());
        replacePrices(saved.getId(), req.prices());
        return toDto(saved);
    }

    public AdminPlanDto update(String id, UpdatePlanRequest req) {
        SubscriptionPlan plan = load(id);
        if (req.name() != null) plan.setName(req.name());
        if (req.pricePerMonth() != null) plan.setPricePerMonth(req.pricePerMonth());
        if (req.currency() != null) plan.setCurrency(req.currency().toUpperCase());
        if (req.active() != null) plan.setActive(req.active());
        if (req.entitlements() != null) {
            plan.setEntitlementsJson(writeEntitlements(req.entitlements()));
            replaceFeatures(id, req.entitlements());
        }
        if (req.prices() != null) replacePrices(id, req.prices());
        return toDto(planRepository.save(plan));
    }

    /** Hides the plan from {@code GET /subscription-plans}. Existing subscriptions are unaffected. */
    public AdminPlanDto deactivate(String id) {
        SubscriptionPlan plan = load(id);
        plan.setActive(false);
        return toDto(planRepository.save(plan));
    }

    // ---- helpers ---------------------------------------------------------------------------

    private void replaceFeatures(String planId, Map<String, Long> entitlements) {
        featureRepository.deleteAll(featureRepository.findByPlanId(planId));
        if (entitlements == null) return;
        entitlements.forEach((key, limit) -> {
            PlanFeature f = new PlanFeature();
            f.setPlanId(planId);
            f.setEntitlementKey(key);
            f.setLimitValue(limit);
            f.setUnlimited(limit == null);
            featureRepository.save(f);
        });
    }

    private void replacePrices(String planId, List<PriceRequest> prices) {
        if (prices == null) return;
        priceRepository.deleteAll(priceRepository.findByPlanId(planId));
        for (PriceRequest p : prices) {
            SubscriptionPlanPrice price = new SubscriptionPlanPrice();
            price.setPlanId(planId);
            price.setCurrency(p.currency().toUpperCase());
            price.setAmount(p.amount());
            price.setBillingPeriod(p.billingPeriod());
            price.setActive(true);
            priceRepository.save(price);
        }
    }

    private String writeEntitlements(Map<String, Long> entitlements) {
        if (entitlements == null || entitlements.isEmpty()) return null;
        Map<String, Long> limited = new LinkedHashMap<>();
        entitlements.forEach((k, v) -> { if (v != null) limited.put(k, v); });
        return jsonMapper.writeValueAsString(limited);
    }

    private Map<String, Long> readEntitlements(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return jsonMapper.readValue(json, new TypeReference<Map<String, Long>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private SubscriptionPlan load(String id) {
        return planRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("SubscriptionPlan", id));
    }

    private AdminPlanDto toDto(SubscriptionPlan p) {
        List<AdminPlanDto.Price> prices = priceRepository.findByPlanId(p.getId()).stream()
                .map(x -> new AdminPlanDto.Price(x.getId(), x.getCurrency(), x.getAmount(),
                        x.getBillingPeriod().name(), x.isActive()))
                .toList();
        return new AdminPlanDto(p.getId(), p.getCode(), p.getName(),
                p.getTargetRole() != null ? p.getTargetRole().name() : null,
                p.getPricePerMonth(), p.getCurrency(), p.isActive(),
                readEntitlements(p.getEntitlementsJson()), prices);
    }
}
