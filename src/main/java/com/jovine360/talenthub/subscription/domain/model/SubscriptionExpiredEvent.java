package com.jovine360.talenthub.subscription.domain.model;

public record SubscriptionExpiredEvent(String subscriptionId, String subscriberId) {}
