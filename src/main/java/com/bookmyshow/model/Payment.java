package com.bookmyshow.model;

import com.bookmyshow.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a payment transaction for a booking.
 * Uses BigDecimal for monetary amounts.
 */
public class Payment {

    private int id;
    private int bookingId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String transactionReference;
    private String failureReason;
    private LocalDateTime paymentTime;

    // --- Constructors ---

    public Payment() {
        this.status = PaymentStatus.PENDING;
        this.paymentTime = LocalDateTime.now();
    }

    public Payment(int bookingId, BigDecimal amount) {
        this();
        this.bookingId = bookingId;
        this.amount = amount;
    }

    public Payment(int id, int bookingId, BigDecimal amount, PaymentStatus status,
                   String transactionReference) {
        this(bookingId, amount);
        this.id = id;
        this.status = status;
        this.transactionReference = transactionReference;
    }

    // --- Getters ---

    public int getId() { return id; }
    public int getBookingId() { return bookingId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getTransactionReference() { return transactionReference; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getPaymentTime() { return paymentTime; }

    // --- Setters ---

    public void setId(int id) { this.id = id; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }

    // --- Business helpers ---

    public boolean isSuccessful() {
        return PaymentStatus.SUCCESS.equals(this.status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment payment = (Payment) o;
        return id == payment.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Payment{id=" + id + ", bookingId=" + bookingId + ", amount=" + amount
                + ", status=" + status + "}";
    }
}
