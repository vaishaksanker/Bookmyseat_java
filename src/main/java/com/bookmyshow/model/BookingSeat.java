package com.bookmyshow.model;

import java.util.Objects;

/**
 * Join entity linking a Booking to a specific Seat.
 */
public class BookingSeat {

    private int id;
    private int bookingId;
    private int seatId;
    private String seatLabel;  // denormalized for display

    // --- Constructors ---

    public BookingSeat() {}

    public BookingSeat(int bookingId, int seatId, String seatLabel) {
        this.bookingId = bookingId;
        this.seatId = seatId;
        this.seatLabel = seatLabel;
    }

    public BookingSeat(int id, int bookingId, int seatId, String seatLabel) {
        this(bookingId, seatId, seatLabel);
        this.id = id;
    }

    // --- Getters ---

    public int getId() { return id; }
    public int getBookingId() { return bookingId; }
    public int getSeatId() { return seatId; }
    public String getSeatLabel() { return seatLabel; }

    // --- Setters ---

    public void setId(int id) { this.id = id; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }
    public void setSeatLabel(String seatLabel) { this.seatLabel = seatLabel; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingSeat)) return false;
        BookingSeat that = (BookingSeat) o;
        return bookingId == that.bookingId && seatId == that.seatId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, seatId);
    }

    @Override
    public String toString() {
        return "BookingSeat{bookingId=" + bookingId + ", seatId=" + seatId + ", label='" + seatLabel + "'}";
    }
}
