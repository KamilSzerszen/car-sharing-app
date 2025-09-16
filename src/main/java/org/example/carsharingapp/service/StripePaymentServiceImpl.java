package org.example.carsharingapp.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripePaymentServiceImpl implements StripePaymentService {

    @Value("${docker.app.port}")
    private String dockerAppPort;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Override
    public Session createCheckoutSession(Long rentalId, Long amount) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        String successUrl = "http://localhost:" + dockerAppPort + "/payments/success";
        String cancelUrl = "http://localhost:" + dockerAppPort + "/payments/cancel";

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams
                                                .LineItem
                                                .PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amount * 100)
                                                .setProductData(
                                                        SessionCreateParams
                                                                .LineItem
                                                                .PriceData
                                                                .ProductData.builder()
                                                                .setName("Rental id: " + rentalId)
                                                                .build())
                                                .build())
                                .build())
                .build();

        return Session.create(params);
    }
}
