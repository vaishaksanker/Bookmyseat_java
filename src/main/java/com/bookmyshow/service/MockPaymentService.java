package com.bookmyshow.service;

import com.bookmyshow.dao.BookingDAO;
import com.bookmyshow.dao.PaymentDAO;
import com.bookmyshow.exception.BookingException;
import com.bookmyshow.exception.PaymentException;
import com.bookmyshow.exception.ValidationException;
import com.bookmyshow.model.Booking;
import com.bookmyshow.model.Payment;
import com.bookmyshow.model.enums.BookingStatus;
import com.bookmyshow.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulated payment service — no real payment gateway.
 *
 * Simulation rule:
 * - Amounts ending in .00 or any valid positive amount → SUCCESS
 * - Amounts ending in .99 → FAILED (simulates a declined card, useful for testing)
 * - Null or negative amount → throws ValidationException
 *
 * In production, replace this class with a real gateway adapter while keeping
 * the same PaymentService interface.
 */
public class MockPaymentService implements PaymentService {

    private final PaymentDAO paymentDAO;
    private final BookingDAO bookingDAO;
    private final BookingService bookingService;

    public MockPaymentService(PaymentDAO paymentDAO,
                               BookingDAO bookingDAO,
                               BookingService bookingService) {
        this.paymentDAO = paymentDAO;
        this.bookingDAO = bookingDAO;
        this.bookingService = bookingService;
    }

    @Override
    public PaymentResult processPayment(int bookingId, BigDecimal amount) {
        // --- Validate amount first ---
        if (amount == null) {
            throw new ValidationException("amount", "Payment amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("amount", "Payment amount must be greater than 0");
        }

        // --- Validate booking ---
        Booking booking = bookingService.getBookingById(bookingId);

        if (BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            throw new PaymentException("Booking " + bookingId + " is already paid and confirmed");
        }
        if (BookingStatus.CANCELLED.equals(booking.getStatus())
                || BookingStatus.FAILED.equals(booking.getStatus())) {
            throw new PaymentException("Cannot pay for booking in status: " + booking.getStatus());
        }

        // --- Verify amount matches booking total ---
        if (booking.getTotalAmount().compareTo(amount) != 0) {
            throw new ValidationException("amount",
                    "Payment amount " + amount + " does not match booking total "
                            + booking.getTotalAmount());
        }

        // --- Check for repeated payment (after amount is validated) ---
        if (paymentDAO.findByBookingId(bookingId).isPresent()) {
            throw new PaymentException("A payment has already been attempted for booking " + bookingId);
        }

        // --- Create Payment record ---
        Payment payment = new Payment(bookingId, amount);
        String txRef = "TXN-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 12);
        payment.setTransactionReference(txRef);

        // --- Simulate payment outcome ---
        boolean paymentSucceeds = simulatePaymentOutcome(amount);

        if (paymentSucceeds) {
            payment.setStatus(PaymentStatus.SUCCESS);
            Payment savedPayment = paymentDAO.create(payment);

            // Confirm the booking
            bookingService.confirmBooking(bookingId);

            return PaymentResult.success(savedPayment);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Simulated payment decline");
            Payment savedPayment = paymentDAO.create(payment);

            // Mark booking as failed and release its reserved seats
            bookingService.failBooking(bookingId);

            return PaymentResult.failure(savedPayment, "Payment was declined (simulated)");
        }
    }

    @Override
    public Payment getPaymentByBookingId(int bookingId) {
        return paymentDAO.findByBookingId(bookingId)
                .orElseThrow(() -> new PaymentException(
                        "No payment found for booking id: " + bookingId));
    }

    /**
     * Simulates payment success/failure.
     *
     * Rule: amounts ending in .99 (fractional part == 99 cents) are declined.
     * All other valid positive amounts succeed.
     *
     * This allows unit tests to deterministically test both paths.
     */
    private boolean simulatePaymentOutcome(BigDecimal amount) {
        // Get cents portion
        BigDecimal cents = amount.remainder(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .abs();
        return cents.compareTo(BigDecimal.valueOf(99)) != 0;
    }
}
