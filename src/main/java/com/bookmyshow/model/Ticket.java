package com.bookmyshow.model;

import com.bookmyshow.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a confirmed digital ticket generated after successful payment.
 */
public class Ticket {

    private int id;
    private String ticketNumber;   // UUID-based human-readable reference
    private int bookingId;
    private int userId;
    private int eventId;
    private String customerName;
    private String eventName;
    private String venueName;
    private LocalDateTime eventDateTime;
    private List<String> seatLabels;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private LocalDateTime generatedAt;

    // --- Constructors ---

    public Ticket() {
        this.ticketNumber = UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 12);
        this.generatedAt = LocalDateTime.now();
        this.seatLabels = new ArrayList<>();
    }

    public Ticket(int bookingId, int userId, int eventId,
                  String customerName, String eventName, String venueName,
                  LocalDateTime eventDateTime, List<String> seatLabels,
                  BigDecimal totalAmount) {
        this();
        this.bookingId = bookingId;
        this.userId = userId;
        this.eventId = eventId;
        this.customerName = customerName;
        this.eventName = eventName;
        this.venueName = venueName;
        this.eventDateTime = eventDateTime;
        this.seatLabels = new ArrayList<>(seatLabels);
        this.totalAmount = totalAmount;
        this.paymentStatus = PaymentStatus.SUCCESS;
    }

    // --- Getters ---

    public int getId() { return id; }
    public String getTicketNumber() { return ticketNumber; }
    public int getBookingId() { return bookingId; }
    public int getUserId() { return userId; }
    public int getEventId() { return eventId; }
    public String getCustomerName() { return customerName; }
    public String getEventName() { return eventName; }
    public String getVenueName() { return venueName; }
    public LocalDateTime getEventDateTime() { return eventDateTime; }
    public List<String> getSeatLabels() { return Collections.unmodifiableList(seatLabels); }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }

    // --- Setters ---

    public void setId(int id) { this.id = id; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public void setEventDateTime(LocalDateTime eventDateTime) { this.eventDateTime = eventDateTime; }
    public void setSeatLabels(List<String> seatLabels) { this.seatLabels = new ArrayList<>(seatLabels); }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticket)) return false;
        Ticket ticket = (Ticket) o;
        return id == ticket.id && Objects.equals(ticketNumber, ticket.ticketNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ticketNumber);
    }

    @Override
    public String toString() {
        return "Ticket{ticketNumber='" + ticketNumber + "', bookingId=" + bookingId
                + ", event='" + eventName + "', seats=" + seatLabels + "}";
    }
}
