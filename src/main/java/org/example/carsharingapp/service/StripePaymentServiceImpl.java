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

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Override
    public Session createCheckoutSession(Long rentalId, Long amount) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        String successUrl = appBaseUrl + "/payments/success";
        String cancelUrl = appBaseUrl + "/payments/cancel";

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
                                                .setUnitAmount(amount)
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
