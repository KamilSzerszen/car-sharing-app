package org.example.carsharingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.carsharingapp.dto.PaymentResponseDto;
import org.example.carsharingapp.dto.PaymentUrlResponseDto;
import org.example.carsharingapp.security.annotation.IsCustomer;
import org.example.carsharingapp.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments controller", description = "Managing payments")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @IsCustomer
    @GetMapping
    @Operation(
            summary = "Get payments",
            description = "For manager get all users payments, for customers only their own"
    )
    public Page<PaymentResponseDto> getPayments(
            @RequestParam(required = false) Long userId,
            Pageable pageable
    ) {
        return paymentService.getPayments(userId, pageable);
    }


    @IsCustomer
    @PostMapping("/{id}")
    @Operation(
            summary = "Create payment session",
            description = "New payment session by rental id"
    )
    public PaymentUrlResponseDto createPaymentSessionByRentalId(@PathVariable Long id) {
        return paymentService.createPaymentSessionByRentalId(id);
    }

    /*
     * Success/cancel endpoints simulate Stripe payments for testing purposes.
     * In production, Stripe webhooks should be used to update payment status.
     * For testing/demo, you need to manually append ?id=PAYMENT_ID after Stripe redirects,
     * because Stripe redirect does not send payment ID or any data to localhost.
     * But for your convenience, I implemented it this way :)
     */

    @GetMapping("/success")
    @Operation(
            summary = "Stripe payment success (test/demo only)",
            description = "Simulates a successful payment for testing/demo purposes. "
                    + "Marks payment as PAID in the database"
    )
    public String stripePaymentSuccess(
            @RequestParam(required = false) Long id
    ) {
        return paymentService.success(id);
    }

    @GetMapping("/cancel")
    @Operation(
            summary = "Stripe payment cancel (test/demo only)",
            description = "Simulates a canceled payment for testing/demo purposes. "
                    + " Payment remains PENDING in the database"
    )
    public String stripePaymentCancelled() {
        return paymentService.cancelled();
    }

}
