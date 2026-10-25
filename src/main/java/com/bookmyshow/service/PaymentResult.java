package com.bookmyshow.service;

import com.bookmyshow.model.Payment;
import com.bookmyshow.model.enums.PaymentStatus;

/**
 * Value object returned by PaymentService.processPayment().
 *
 * Encapsulates the outcome of a payment attempt so the caller does not
 * need to inspect Payment internals directly.
 */
public class PaymentResult {

    private final Payment payment;
    private final boolean success;
    private final String message;

    private PaymentResult(Payment payment, boolean success, String message) {
        this.payment = payment;
        this.success = success;
        this.message = message;
    }

    // --- Factory methods ---

    public static PaymentResult success(Payment payment) {
        return new PaymentResult(payment, true, "Payment successful");
    }

    public static PaymentResult failure(Payment payment, String reason) {
        return new PaymentResult(payment, false, reason);
    }

    // --- Accessors ---

    public Payment getPayment() { return payment; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    public PaymentStatus getStatus() {
        return payment != null ? payment.getStatus() : PaymentStatus.FAILED;
    }

    @Override
    public String toString() {
        return "PaymentResult{success=" + success + ", message='" + message + "'}";
    }
}
