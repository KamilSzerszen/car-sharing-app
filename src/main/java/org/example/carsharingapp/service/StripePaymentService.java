package org.example.carsharingapp.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

public interface StripePaymentService {
    Session createCheckoutSession(Long rentalId, Long amount) throws StripeException;
}
