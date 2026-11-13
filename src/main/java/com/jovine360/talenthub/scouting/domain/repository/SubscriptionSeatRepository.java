package com.jovine360.talenthub.scouting.domain.repository;

import com.jovine360.talenthub.scouting.domain.model.SubscriptionSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionSeatRepository extends JpaRepository<SubscriptionSeat, String> {
    List<SubscriptionSeat> findBySubscriptionIdAndRevokedAtIsNull(String subscriptionId);
    long countBySubscriptionIdAndRevokedAtIsNull(String subscriptionId);
}
