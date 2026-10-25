package com.bookmyshow.model;

import com.bookmyshow.model.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a booking made by a user for one or more seats at an event.
 * Demonstrates collection usage and immutability of seat list exposure.
 */
public class Booking {

    private int id;
    private int userId;
    private int eventId;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime bookingTime;
    private List<BookingSeat> bookingSeats;

    // --- Constructors ---

    public Booking() {
        this.status = BookingStatus.PENDING;
        this.bookingTime = LocalDateTime.now();
        this.bookingSeats = new ArrayList<>();
    }

    public Booking(int id, int userId, int eventId, BigDecimal totalAmount) {
        this();
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.totalAmount = totalAmount;
    }

    // --- Getters ---

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getEventId() { return eventId; }
    public BookingStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getBookingTime() { return bookingTime; }

    /** Returns an unmodifiable view of booking seats. */
    public List<BookingSeat> getBookingSeats() {
        return Collections.unmodifiableList(bookingSeats);
    }

    // --- Setters ---

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    // --- Collection management ---

    public void addBookingSeat(BookingSeat bookingSeat) {
        this.bookingSeats.add(bookingSeat);
    }

    public void setBookingSeats(List<BookingSeat> bookingSeats) {
        this.bookingSeats = new ArrayList<>(bookingSeats);
    }

    // --- Business helpers ---

    public boolean isConfirmed() {
        return BookingStatus.CONFIRMED.equals(this.status);
    }

    public boolean isPending() {
        return BookingStatus.PENDING.equals(this.status);
    }

    public int getSeatCount() {
        return bookingSeats.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return id == booking.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Booking{id=" + id + ", userId=" + userId + ", eventId=" + eventId
                + ", status=" + status + ", total=" + totalAmount + "}";
    }
}
