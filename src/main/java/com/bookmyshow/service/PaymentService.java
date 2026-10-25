package com.bookmyshow.service;

import com.bookmyshow.model.Payment;

import java.math.BigDecimal;

/**
 * Service interface for payment processing.
 *
 * Payment is simulated. No real gateway is used.
 * Flow: Booking → processPayment() → success → confirmBooking + generateTicket
 *                                  → failure → booking stays PENDING or FAILED
 */
public interface PaymentService {

    /**
     * Processes a payment for the given booking.
     *
     * Validates the amount against the booking total. Creates a Payment record.
     * On success: transitions booking to CONFIRMED.
     * On failure: marks Payment as FAILED, booking remains PENDING.
     *
     * @throws com.bookmyshow.exception.PaymentException  if the booking is invalid or payment already exists
     * @throws com.bookmyshow.exception.ValidationException if the amount is invalid
     */
    PaymentResult processPayment(int bookingId, BigDecimal amount);

    /**
     * Retrieves a payment by booking ID.
     *
     * @throws com.bookmyshow.exception.PaymentException if no payment exists for the booking
     */
    Payment getPaymentByBookingId(int bookingId);
}
