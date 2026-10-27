package com.seminarhall.model;

import java.io.Serializable;
import java.util.Objects;

public class Seat implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String eventId;
    private String rowLabel;
    private int seatNumber;
    private SeatStatus status;
    private double price;
    private boolean accessible;

    public Seat() {
    }

    public Seat(String id, String eventId, String rowLabel, int seatNumber, SeatStatus status, double price, boolean accessible) {
        this.id = id;
        this.eventId = eventId;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.status = status;
        this.price = price;
        this.accessible = accessible;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    public String getSeatIdentifier() {
        return rowLabel + seatNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return Objects.equals(id, seat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getSeatIdentifier() + " (" + status + ")";
    }
}
