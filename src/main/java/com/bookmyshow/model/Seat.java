package com.bookmyshow.model;

import com.bookmyshow.model.enums.SeatStatus;

import java.util.Objects;

/**
 * Represents a physical seat in a venue, associated with an event.
 */
public class Seat {

    private int id;
    private int venueId;
    private int eventId;
    private String seatRow;
    private int seatNumber;
    private String seatLabel; // e.g. "A10"
    private SeatStatus status;

    // --- Constructors ---

    public Seat() {
        this.status = SeatStatus.AVAILABLE;
    }

    public Seat(int id, int venueId, int eventId, String seatRow, int seatNumber) {
        this();
        this.id = id;
        this.venueId = venueId;
        this.eventId = eventId;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.seatLabel = seatRow + seatNumber;
    }

    // --- Getters ---

    public int getId() { return id; }
    public int getVenueId() { return venueId; }
    public int getEventId() { return eventId; }
    public String getSeatRow() { return seatRow; }
    public int getSeatNumber() { return seatNumber; }
    public String getSeatLabel() { return seatLabel; }
    public SeatStatus getStatus() { return status; }

    // --- Setters ---

    public void setId(int id) { this.id = id; }
    public void setVenueId(int venueId) { this.venueId = venueId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public void setSeatRow(String seatRow) { this.seatRow = seatRow; updateLabel(); }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; updateLabel(); }
    public void setSeatLabel(String seatLabel) { this.seatLabel = seatLabel; }
    public void setStatus(SeatStatus status) { this.status = status; }

    // --- Business helpers ---

    public boolean isAvailable() {
        return SeatStatus.AVAILABLE.equals(this.status);
    }

    private void updateLabel() {
        if (seatRow != null) {
            this.seatLabel = seatRow + seatNumber;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat)) return false;
        Seat seat = (Seat) o;
        return id == seat.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Seat{id=" + id + ", label='" + seatLabel + "', eventId=" + eventId + ", status=" + status + "}";
    }
}
