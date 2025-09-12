package org.example.carsharingapp.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.example.carsharingapp.dto.PaymentResponseDto;
import org.example.carsharingapp.dto.PaymentUrlResponseDto;
import org.example.carsharingapp.exception.AccessDeniedException;
import org.example.carsharingapp.exception.EntityNotFoundException;
import org.example.carsharingapp.exception.PaymentException;
import org.example.carsharingapp.exception.StripeSessionException;
import org.example.carsharingapp.mapper.PaymentMapper;
import org.example.carsharingapp.model.Payment;
import org.example.carsharingapp.model.PaymentStatus;
import org.example.carsharingapp.model.PaymentStatusName;
import org.example.carsharingapp.model.PaymentType;
import org.example.carsharingapp.model.PaymentTypeName;
import org.example.carsharingapp.model.Rental;
import org.example.carsharingapp.model.Role;
import org.example.carsharingapp.model.RoleName;
import org.example.carsharingapp.model.User;
import org.example.carsharingapp.repository.PaymentRepository;
import org.example.carsharingapp.repository.PaymentStatusRepository;
import org.example.carsharingapp.repository.PaymentTypeRepository;
import org.example.carsharingapp.repository.RentalRepository;
import org.example.carsharingapp.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final static BigDecimal FINE_DAILY_MULTIPLIER = new BigDecimal("0.50");

    private final RentalRepository rentalRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final StripePaymentService stripePaymentService;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentUrlResponseDto createPaymentSessionByRentalId(Long id) {
        Rental rentalById = getRentalById(id);
        User currentUser = userService.getCurrentUser();
        Role managerRole = getUserRole(RoleName.ROLE_MANAGER);

        if (rentalById.getActualReturnDate() == null) {
            throw new PaymentException(
                    "PaymentService: Car has not been returned"
            );
        }
        if (
                rentalById.getUser().getId() != currentUser.getId()
                        && !currentUser.getRoles().contains(managerRole)
        ) {
            throw new AccessDeniedException(
                    "PaymentService: Rental with id " + id + " not exist for current user"
            );
        }

        BigDecimal dailyPrice = rentalById.getCar().getDailyPrice();
        LocalDateTime rentalDate = rentalById.getRentalDate();
        LocalDateTime returnDate = rentalById.getReturnDate();
        LocalDateTime actualReturnDate = rentalById.getActualReturnDate();
        boolean isOverdue = actualReturnDate.isAfter(returnDate);

        BigDecimal amount = getAmount(
                isOverdue, dailyPrice, rentalDate, returnDate, actualReturnDate
        );

        long amountInCents = amount.multiply(BigDecimal.valueOf(100.0)).longValueExact();

        Session session;

        try {
            session = getStripeSession(rentalById.getId(), amountInCents);
        } catch (StripeException e) {
            throw new StripeSessionException(
                    "PaymentService: Can't make Stripe session for rental with id " + id
            );
        }


        Payment newPayment = new Payment();
        newPayment.setStatus(getPaymentStatus(PaymentStatusName.PENDING));
        newPayment.setType(
                isOverdue
                        ? getPaymentType(PaymentTypeName.FINE)
                        : getPaymentType(PaymentTypeName.PAYMENT)
        );
        newPayment.setRental(rentalById);
        newPayment.setSessionUrl(session.getUrl());
        newPayment.setSessionId(session.getId());
        newPayment.setAmountToPay(amount);

        Payment saved = paymentRepository.save(newPayment);
        return new PaymentUrlResponseDto(saved.getSessionUrl());
    }

    @Override
    @Transactional
    public Page<PaymentResponseDto> getPayments(Long userId, Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        boolean isManager = currentUser.getRoles().contains(getUserRole(RoleName.ROLE_MANAGER));

        if (!isManager) {
            if (userId == null || currentUser.getId() != userId) {
                userId = currentUser.getId();
            }

            Page<Payment> paymentsByUserId = paymentRepository.findAllByUserId(userId, pageable);
            return paymentsByUserId.map(paymentMapper::toDto);
        }

        if (userId == null) {
            Page<Payment> paymentsAll = paymentRepository.findAll(pageable);
            return paymentsAll.map(paymentMapper::toDto);
        }

        Page<Payment> paymentsByUserId = paymentRepository.findAllByUserId(userId, pageable);
        return paymentsByUserId.map(paymentMapper::toDto);
    }

    @Override
    @Transactional
    public String success(Long id) {
        if (id != null) {
            Payment payment = getPayment(id);
            payment.setStatus(getPaymentStatus(PaymentStatusName.PAID));
            paymentRepository.save(payment);
            return "Payment successful! Payment with id " + id
                    + " change on PAID status (Demo mode)";
        }
        return "Payment successful! (Demo mode)";
    }

    @Override
    public String cancelled() {
        return "Payment cancelled";
    }

    private Payment getPayment(Long id) {
        return paymentRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        "PaymentService: Payment with id " + id + " not found"
                ));
    }

    private PaymentStatus getPaymentStatus(PaymentStatusName paymentStatusName) {
        return paymentStatusRepository.findByPaymentStatusName(paymentStatusName).orElseThrow(
                () -> new EntityNotFoundException(
                        "PaymentService: Payment status " + paymentStatusName + " not found"
                ));
    }

    private Role getUserRole(RoleName roleName) {
        return roleRepository.findByName(roleName).orElseThrow(
                () -> new EntityNotFoundException(
                        "PaymentService: Role with name " + roleName + " not found"
                ));
    }

    private Rental getRentalById(Long id) {
        return rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        "PaymentService: Rental with id " + id + " not found"
                ));
    }

    private PaymentType getPaymentType(PaymentTypeName paymentTypeName) {
        return paymentTypeRepository.findByPaymentTypeName(paymentTypeName).orElseThrow(
                () -> new EntityNotFoundException(
                        "PaymentService: Payment type " + paymentTypeName + " not found"
                ));
    }

    private BigDecimal getAmount(
            boolean isOverdue,
            BigDecimal dailyPrice,
            LocalDateTime rentalDate,
            LocalDateTime returnDate,
            LocalDateTime actualReturnDate) {

        if (isOverdue) {
            long rentalDays = ChronoUnit.DAYS.between(
                    rentalDate.toLocalDate(), returnDate.toLocalDate());
            long overdueDays = ChronoUnit.DAYS.between(
                    returnDate.toLocalDate(), actualReturnDate.toLocalDate());

            BigDecimal basicAmount = dailyPrice.multiply(BigDecimal.valueOf(rentalDays));
            BigDecimal fineAmount = (dailyPrice
                    .multiply(BigDecimal.valueOf(overdueDays)))
                    .multiply(FINE_DAILY_MULTIPLIER);

            return basicAmount.add(fineAmount);
        }

        long rentalDays = ChronoUnit.DAYS.between(
                rentalDate.toLocalDate(), returnDate.toLocalDate());

        return dailyPrice.multiply(BigDecimal.valueOf(rentalDays));
    }

    private Session getStripeSession(Long rentalId, Long amountInCents) throws StripeException {
        return stripePaymentService.createCheckoutSession(rentalId, amountInCents);
    }
}
