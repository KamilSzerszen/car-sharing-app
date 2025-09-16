package org.example.carsharingapp.exception;

public class StripeSessionException extends RuntimeException {
    public StripeSessionException(String message) {
        super(message);
    }
}
