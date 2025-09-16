package org.example.carsharingapp.service;

import org.example.carsharingapp.dto.PaymentResponseDto;
import org.example.carsharingapp.dto.PaymentUrlResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentUrlResponseDto createPaymentSessionByRentalId(Long id);

    Page<PaymentResponseDto> getPayments(Long userId, Pageable pageable);

    String success(Long id);

    String cancelled();
}
