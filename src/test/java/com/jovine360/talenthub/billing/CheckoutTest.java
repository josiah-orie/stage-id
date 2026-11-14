package com.jovine360.talenthub.billing;

import com.jovine360.talenthub.billing.application.service.PaymentConfirmedEvent;
import com.jovine360.talenthub.billing.application.service.PaymentConfirmedListener;
import com.jovine360.talenthub.billing.application.service.PaymentGatewayProvider;
import com.jovine360.talenthub.billing.domain.model.PaymentTransaction;
import com.jovine360.talenthub.billing.domain.repository.PaymentTransactionRepository;
import com.jovine360.talenthub.billing.infrastructure.web.CheckoutController;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import com.jovine360.talenthub.subscription.api.SubscriptionApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Checkout starts a payment for a pending subscription; a confirmed payment activates it. */
class CheckoutTest {

    private static final AuthenticatedPrincipal SCOUT = AuthenticatedPrincipal.of("u1", "u1@x.com", List.of("SCOUT"));

    @Test
    void checkoutCreatesPendingTransactionForPendingPaidSubscription() {
        SubscriptionApi subs = mock(SubscriptionApi.class);
        when(subs.loadOwned("sub-1", "u1", SCOUT.roles())).thenReturn(new SubscriptionApi.SubscriptionSummary(
                "sub-1", "u1", "PENDING", "SCOUT_PRO", new BigDecimal("29.00"), "USD"));
        PaymentGatewayProvider gateway = mock(PaymentGatewayProvider.class);
        when(gateway.initializeTransaction(any(), any(), any(), any()))
                .thenReturn(new PaymentGatewayProvider.PaymentInitResponse("ref-1", "https://pay/abc", "abc"));
        PaymentTransactionRepository txs = mock(PaymentTransactionRepository.class);
        when(txs.save(any())).thenAnswer(i -> { PaymentTransaction t = i.getArgument(0); t.setId("tx-1"); return t; });
        var controller = new CheckoutController(subs, gateway, txs, "https://cb", "paystack");

        var body = controller.checkout("sub-1", SCOUT).getBody();
        assertThat(body.authorizationUrl()).isEqualTo("https://pay/abc");
        ArgumentCaptor<PaymentTransaction> captor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(txs).save(captor.capture());
        assertThat(captor.getValue().getSubscriptionId()).isEqualTo("sub-1");
        assertThat(captor.getValue().getProviderReference()).isEqualTo("ref-1");
        assertThat(captor.getValue().getProviderCode()).isEqualTo("PAYSTACK");
    }

    @Test
    void activeOrFreeSubscriptionCannotCheckOut() {
        SubscriptionApi subs = mock(SubscriptionApi.class);
        when(subs.loadOwned("active", "u1", SCOUT.roles())).thenReturn(new SubscriptionApi.SubscriptionSummary(
                "active", "u1", "ACTIVE", "SCOUT_PRO", new BigDecimal("29.00"), "USD"));
        when(subs.loadOwned("free", "u1", SCOUT.roles())).thenReturn(new SubscriptionApi.SubscriptionSummary(
                "free", "u1", "PENDING", "FREE_TIER", BigDecimal.ZERO, "USD"));
        var controller = new CheckoutController(subs, mock(PaymentGatewayProvider.class),
                mock(PaymentTransactionRepository.class), "https://cb", "paystack");

        assertThatThrownBy(() -> controller.checkout("active", SCOUT)).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(() -> controller.checkout("free", SCOUT)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void confirmedPaymentActivatesSubscription() {
        SubscriptionApi subs = mock(SubscriptionApi.class);
        new PaymentConfirmedListener(subs).onPaymentConfirmed(
                new PaymentConfirmedEvent("tx-1", "sub-1", "u1", new BigDecimal("29.00"), "USD"));
        verify(subs).activate("sub-1");
    }
}
