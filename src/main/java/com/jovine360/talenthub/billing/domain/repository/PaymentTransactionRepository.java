package com.jovine360.talenthub.billing.domain.repository;

import com.jovine360.talenthub.billing.domain.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    Optional<PaymentTransaction> findByProviderReference(String providerReference);
}
