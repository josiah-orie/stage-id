package com.jovine360.talenthub.billing.domain.repository;

import com.jovine360.talenthub.billing.domain.model.PaymentProviderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentProviderEventRepository extends JpaRepository<PaymentProviderEvent, String> {
    Optional<PaymentProviderEvent> findByProviderCodeAndProviderEventId(
            String providerCode, String providerEventId);
}
