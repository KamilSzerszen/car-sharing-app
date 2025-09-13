package org.example.carsharingapp.mapper;

import org.example.carsharingapp.dto.PaymentResponseDto;
import org.example.carsharingapp.exception.EntityNotFoundException;
import org.example.carsharingapp.model.Payment;
import org.example.carsharingapp.model.Rental;
import org.example.carsharingapp.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class PaymentMapper {

    @Autowired
    private RentalRepository rentalRepository;

    @Transactional
    public PaymentResponseDto toDto(Payment payment) {
        Rental rental = payment.getRental();

        Long id = payment.getId();
        String status = payment.getStatus().getPaymentStatusName().name();
        String type = payment.getType().getPaymentTypeName().name();
        String brand = rental.getCar().getBrand();
        String model = rental.getCar().getModel();
        String sessionUrl = payment.getSessionUrl();
        String sessionId = payment.getSessionId();
        BigDecimal amountToPay = payment.getAmountToPay();

        return new PaymentResponseDto(
                id, status, type, brand, model, sessionUrl, sessionId, amountToPay
        );
    }
}
