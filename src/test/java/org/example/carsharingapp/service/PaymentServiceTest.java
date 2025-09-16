package org.example.carsharingapp.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.example.carsharingapp.dto.PaymentResponseDto;
import org.example.carsharingapp.dto.PaymentUrlResponseDto;
import org.example.carsharingapp.exception.AccessDeniedException;
import org.example.carsharingapp.exception.PaymentException;
import org.example.carsharingapp.mapper.PaymentMapper;
import org.example.carsharingapp.model.Car;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private UserService userService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PaymentTypeRepository paymentTypeRepository;

    @Mock
    private PaymentStatusRepository paymentStatusRepository;

    @Mock
    private StripePaymentService stripePaymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Should create Stripe payment session when rental returned and user is owner")
    public void shouldCreatePaymentSession_whenRentalReturnedAndUserIsOwner_returnPaymentUrl()
            throws StripeException {
        Long rentalId = 1L;

        User user = new User();
        user.setId(1L);

        Role managerRole = new Role();
        managerRole.setName(RoleName.ROLE_MANAGER);

        Car car = new Car();
        car.setDailyPrice(BigDecimal.TEN);
        car.setBrand("Toyota");
        car.setModel("Corolla");

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setUser(user);
        rental.setCar(car);
        rental.setRentalDate(LocalDateTime.now().minusDays(2));
        rental.setReturnDate(LocalDateTime.now().minusDays(1));
        rental.setActualReturnDate(LocalDateTime.now());

        Session session = new Session();
        session.setId("sess_123");
        session.setUrl("https://stripe.com/session");

        PaymentStatus pendingStatus = new PaymentStatus();
        pendingStatus.setPaymentStatusName(PaymentStatusName.PENDING);

        PaymentType paymentTypeFine = new PaymentType();
        paymentTypeFine.setPaymentTypeName(PaymentTypeName.FINE);

        Mockito.when(rentalRepository
                .findById(rentalId))
                .thenReturn(Optional.of(rental));
        Mockito.when(userService
                .getCurrentUser())
                .thenReturn(user);
        Mockito.when(roleRepository
                .findByName(RoleName.ROLE_MANAGER))
                .thenReturn(Optional.of(managerRole));
        Mockito.when(stripePaymentService
                .createCheckoutSession(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(session);
        Mockito.when(paymentStatusRepository
                .findByPaymentStatusName(PaymentStatusName.PENDING))
                .thenReturn(Optional.of(pendingStatus));
        Mockito.when(paymentTypeRepository
                .findByPaymentTypeName(PaymentTypeName.FINE))
                .thenReturn(Optional.of(paymentTypeFine));
        Mockito.when(paymentRepository
                .save(Mockito.any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentUrlResponseDto result = paymentService.createPaymentSessionByRentalId(rentalId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(session.getUrl(), result.stripeUrl());
    }


    @Test
    @DisplayName("Should throw PaymentException when rental not returned")
    public void shouldThrowPaymentException_whenRentalNotReturned() {
        Rental rental = new Rental();
        rental.setActualReturnDate(null);
        rental.setUser(new User());

        Mockito.when(rentalRepository
                .findById(1L))
                .thenReturn(Optional.of(rental));
        Mockito.when(userService
                .getCurrentUser())
                .thenReturn(new User());
        Mockito.when(roleRepository
                .findByName(RoleName.ROLE_MANAGER))
                .thenReturn(Optional.of(new Role()));

        Assertions.assertThrows(PaymentException.class,
                () -> paymentService.createPaymentSessionByRentalId(1L));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when user is not owner or manager")
    public void shouldThrowAccessDeniedException_whenUserNotOwnerOrManager() {
        User rentalOwner = new User();
        rentalOwner.setId(1L);

        User currentUser = new User();
        currentUser.setId(2L);
        currentUser.setRoles(Collections.emptySet());

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUser(rentalOwner);
        rental.setActualReturnDate(LocalDateTime.now());

        Mockito.when(rentalRepository
                .findById(1L))
                .thenReturn(Optional.of(rental));
        Mockito.when(userService
                .getCurrentUser())
                .thenReturn(currentUser);
        Mockito.when(roleRepository
                .findByName(RoleName.ROLE_MANAGER))
                .thenReturn(Optional.of(new Role()));

        Assertions.assertThrows(AccessDeniedException.class,
                () -> paymentService.createPaymentSessionByRentalId(1L));
    }

    @Test
    @DisplayName("Should throw StripeSessionException when Stripe fails")
    public void shouldThrowStripeSessionException_whenStripeThrows() throws StripeException {
        Long rentalId = 1L;

        User user = new User();
        user.setId(1L);
        user.setRoles(Set.of());

        Car car = new Car();
        car.setDailyPrice(BigDecimal.TEN);
        car.setBrand("Toyota");
        car.setModel("Corolla");

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setUser(user);
        rental.setCar(car);
        rental.setRentalDate(LocalDateTime.now().minusDays(2));
        rental.setReturnDate(LocalDateTime.now().minusDays(1));
        rental.setActualReturnDate(LocalDateTime.now());

        Mockito.when(rentalRepository
                .findById(rentalId))
                .thenReturn(Optional.of(rental));
        Mockito.when(userService
                .getCurrentUser())
                .thenReturn(user);
        Mockito.when(roleRepository
                .findByName(RoleName.ROLE_MANAGER))
                .thenReturn(Optional.of(new Role()));

        Mockito.when(stripePaymentService
                .createCheckoutSession(Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new PaymentException("Exception"));

        Assertions.assertThrows(PaymentException.class,
                () -> paymentService.createPaymentSessionByRentalId(rentalId));
    }


    @Test
    @DisplayName("Should return 'Payment successful!' with id when success is called with id")
    public void shouldReturnSuccessMessage_whenSuccessCalledWithId() {
        Payment payment = new Payment();
        payment.setId(1L);

        PaymentStatus paidStatus = new PaymentStatus();
        paidStatus.setPaymentStatusName(PaymentStatusName.PAID);

        Mockito.when(paymentRepository
                .findById(1L))
                .thenReturn(Optional.of(payment));
        Mockito.when(paymentStatusRepository
                .findByPaymentStatusName(PaymentStatusName.PAID))
                .thenReturn(Optional.of(paidStatus));
        Mockito.when(paymentRepository
                .save(Mockito.any(Payment.class)))
                .thenReturn(payment);

        String result = paymentService.success(1L);

        Assertions.assertTrue(result.contains("Payment successful!"));
        Assertions.assertTrue(result.contains("1"));
    }

    @Test
    @DisplayName("Should return 'Payment successful!' demo message when success called with null")
    public void shouldReturnDemoSuccessMessage_whenSuccessCalledWithNull() {
        String result = paymentService.success(null);
        Assertions.assertEquals("Payment successful! (Demo mode)", result);
    }

    @Test
    @DisplayName("Should return 'Payment cancelled' when cancelled called")
    public void shouldReturnCancelledMessage_whenCancelledCalled() {
        String result = paymentService.cancelled();
        Assertions.assertEquals("Payment cancelled", result);
    }

    @Test
    @DisplayName("Should return payments page for manager")
    public void shouldReturnAllPayments_whenUserIsManager() {
        User manager = new User();
        Role managerRole = new Role();
        managerRole.setName(RoleName.ROLE_MANAGER);
        manager.setRoles(Set.of(managerRole));

        Payment payment = new Payment();
        Page<Payment> page = new PageImpl<>(List.of(payment));

        Mockito.when(userService
                .getCurrentUser())
                .thenReturn(manager);
        Mockito.when(roleRepository
                .findByName(RoleName.ROLE_MANAGER))
                .thenReturn(Optional.of(managerRole));
        Mockito.when(paymentRepository
                .findAll(Pageable.ofSize(5)))
                .thenReturn(page);
        Mockito.when(paymentMapper
                .toDto(payment))
                .thenReturn(new PaymentResponseDto(
                        1L,
                        "PENDING",
                        "PAYMENT",
                        "Toyota",
                        "Corolla",
                        "url",
                        "sess",
                        BigDecimal.TEN));

        Page<PaymentResponseDto> result = paymentService.getPayments(
                null, Pageable.ofSize(5));

        Assertions.assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should return payments page for customer")
    public void shouldReturnPaymentsByUserId_whenUserIsCustomer() {
        User customer = new User();
        customer.setId(1L);
        customer.setRoles(Collections.emptySet());

        Payment payment = new Payment();
        Page<Payment> page = new PageImpl<>(List.of(payment));

        Mockito.when(userService
                .getCurrentUser())
                .thenReturn(customer);
        Mockito.when(roleRepository
                .findByName(RoleName.ROLE_MANAGER))
                .thenReturn(Optional.of(new Role()));
        Mockito.when(paymentRepository
                .findAllByUserId(1L, Pageable.ofSize(5)))
                .thenReturn(page);
        Mockito.when(paymentMapper
                .toDto(payment))
                .thenReturn(new PaymentResponseDto(
                        1L,
                        "PENDING",
                        "PAYMENT",
                        "Toyota",
                        "Corolla",
                        "url",
                        "sess",
                        BigDecimal.TEN
                )
        );

        Page<PaymentResponseDto> result = paymentService.getPayments(
                1L, Pageable.ofSize(5));

        Assertions.assertEquals(1, result.getTotalElements());
    }

}
