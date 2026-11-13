package com.jovine360.talenthub.billing.domain.repository;

import com.jovine360.talenthub.billing.domain.model.BillingInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingInvoiceRepository extends JpaRepository<BillingInvoice, String> {
    List<BillingInvoice> findByUserId(String userId);
}
