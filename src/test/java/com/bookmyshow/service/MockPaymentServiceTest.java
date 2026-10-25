package com.bookmyshow.service;

import com.bookmyshow.dao.BookingDAO;
import com.bookmyshow.dao.PaymentDAO;
import com.bookmyshow.exception.PaymentException;
import com.bookmyshow.exception.ValidationException;
import com.bookmyshow.model.Booking;
import com.bookmyshow.model.Payment;
import com.bookmyshow.model.enums.BookingStatus;
import com.bookmyshow.model.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MockPaymentService Unit Tests")
class MockPaymentServiceTest {

    @Mock private PaymentDAO paymentDAO;
    @Mock private BookingDAO bookingDAO;
    @Mock private BookingService bookingService;

    private MockPaymentService paymentService;

    private Booking pendingBooking;

    @BeforeEach
    void setUp() {
        paymentService = new MockPaymentService(paymentDAO, bookingDAO, bookingService);

        pendingBooking = new Booking(1, 10, 5, new BigDecimal("400.00"));
        // status is PENDING by default
    }

    // =========================================================
    // Successful payment
    // =========================================================

    @Test
    @DisplayName("processPayment: valid amount succeeds and confirms booking")
    void processPayment_validAmount_returnsSuccess() {
        when(bookingService.getBookingById(1)).thenReturn(pendingBooking);
        when(paymentDAO.findByBookingId(1)).thenReturn(Optional.empty());

        Payment savedPayment = new Payment(1, 1, new BigDecimal("400.00"),
                PaymentStatus.SUCCESS, "TXN-ABC123");
        when(paymentDAO.create(any())).thenReturn(savedPayment);

        PaymentResult result = paymentService.processPayment(1, new BigDecimal("400.00"));

        assertTrue(result.isSuccess());
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        verify(bookingService).confirmBooking(1);
        verify(paymentDAO).create(any());
    }

    // =========================================================
    // Simulated payment failure (.99 cents rule)
    // =========================================================

    @Test
    @DisplayName("processPayment: amount ending in .99 simulates decline")
    void processPayment_amountEndsIn99_returnsFailure() {
        Booking booking99 = new Booking(2, 10, 5, new BigDecimal("399.99"));
        when(bookingService.getBookingById(2)).thenReturn(booking99);
        when(paymentDAO.findByBookingId(2)).thenReturn(Optional.empty());

        Payment failedPayment = new Payment(2, 2, new BigDecimal("399.99"),
                PaymentStatus.FAILED, "TXN-FAIL");
        when(paymentDAO.create(any())).thenReturn(failedPayment);
        when(bookingDAO.update(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResult result = paymentService.processPayment(2, new BigDecimal("399.99"));

        assertFalse(result.isSuccess());
        assertEquals(PaymentStatus.FAILED, result.getStatus());
        verify(bookingService, never()).confirmBooking(anyInt());
        // booking should be marked FAILED
        verify(bookingDAO).update(argThat(b -> BookingStatus.FAILED.equals(b.getStatus())));
    }

    // =========================================================
    // Invalid amount
    // =========================================================

    @Test
    @DisplayName("processPayment: null amount throws ValidationException")
    void processPayment_nullAmount_throwsValidation() {
        assertThrows(ValidationException.class, () ->
                paymentService.processPayment(1, null));
        verify(paymentDAO, never()).create(any());
    }

    @Test
    @DisplayName("processPayment: zero amount throws ValidationException")
    void processPayment_zeroAmount_throwsValidation() {
        assertThrows(ValidationException.class, () ->
                paymentService.processPayment(1, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("processPayment: negative amount throws ValidationException")
    void processPayment_negativeAmount_throwsValidation() {
        assertThrows(ValidationException.class, () ->
                paymentService.processPayment(1, new BigDecimal("-50.00")));
    }

    @Test
    @DisplayName("processPayment: amount mismatch throws ValidationException")
    void processPayment_amountMismatch_throwsValidation() {
        when(bookingService.getBookingById(1)).thenReturn(pendingBooking); // total=400.00

        assertThrows(ValidationException.class, () ->
                paymentService.processPayment(1, new BigDecimal("350.00")));
        verify(paymentDAO, never()).create(any());
    }

    // =========================================================
    // Invalid booking states
    // =========================================================

    @Test
    @DisplayName("processPayment: already CONFIRMED booking throws PaymentException")
    void processPayment_confirmedBooking_throwsPaymentException() {
        pendingBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingService.getBookingById(1)).thenReturn(pendingBooking);

        assertThrows(PaymentException.class, () ->
                paymentService.processPayment(1, new BigDecimal("400.00")));
    }

    @Test
    @DisplayName("processPayment: CANCELLED booking throws PaymentException")
    void processPayment_cancelledBooking_throwsPaymentException() {
        pendingBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingService.getBookingById(1)).thenReturn(pendingBooking);

        assertThrows(PaymentException.class, () ->
                paymentService.processPayment(1, new BigDecimal("400.00")));
    }

    @Test
    @DisplayName("processPayment: FAILED booking throws PaymentException")
    void processPayment_failedBooking_throwsPaymentException() {
        pendingBooking.setStatus(BookingStatus.FAILED);
        when(bookingService.getBookingById(1)).thenReturn(pendingBooking);

        assertThrows(PaymentException.class, () ->
                paymentService.processPayment(1, new BigDecimal("400.00")));
    }

    // =========================================================
    // Repeated payment attempt
    // =========================================================

    @Test
    @DisplayName("processPayment: repeated attempt throws PaymentException")
    void processPayment_repeatedAttempt_throwsPaymentException() {
        when(bookingService.getBookingById(1)).thenReturn(pendingBooking);
        // Payment already exists for this booking
        Payment existingPayment = new Payment(1, 1, new BigDecimal("400.00"),
                PaymentStatus.SUCCESS, "TXN-OLD");
        when(paymentDAO.findByBookingId(1)).thenReturn(Optional.of(existingPayment));

        assertThrows(PaymentException.class, () ->
                paymentService.processPayment(1, new BigDecimal("400.00")));
        verify(paymentDAO, never()).create(any());
    }

    // =========================================================
    // getPaymentByBookingId
    // =========================================================

    @Test
    @DisplayName("getPaymentByBookingId: existing payment is returned")
    void getPaymentByBookingId_found_returnsPayment() {
        Payment payment = new Payment(1, 1, new BigDecimal("400.00"),
                PaymentStatus.SUCCESS, "TXN-XYZ");
        when(paymentDAO.findByBookingId(1)).thenReturn(Optional.of(payment));

        Payment result = paymentService.getPaymentByBookingId(1);
        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
    }

    @Test
    @DisplayName("getPaymentByBookingId: not found throws PaymentException")
    void getPaymentByBookingId_notFound_throwsPaymentException() {
        when(paymentDAO.findByBookingId(99)).thenReturn(Optional.empty());
        assertThrows(PaymentException.class, () ->
                paymentService.getPaymentByBookingId(99));
    }
}
