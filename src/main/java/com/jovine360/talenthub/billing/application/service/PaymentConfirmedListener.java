package com.jovine360.talenthub.billing.application.service;

import com.jovine360.talenthub.subscription.api.SubscriptionApi;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** A confirmed payment activates the subscription it paid for. */
@Component
public class PaymentConfirmedListener {

    private final SubscriptionApi subscriptionApi;

    public PaymentConfirmedListener(SubscriptionApi subscriptionApi) {
        this.subscriptionApi = subscriptionApi;
    }

    @EventListener
    public void onPaymentConfirmed(PaymentConfirmedEvent event) {
        if (event.subscriptionId() != null) {
            subscriptionApi.activate(event.subscriptionId());
        }
    }
}
