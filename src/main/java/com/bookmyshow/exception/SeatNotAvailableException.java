package com.bookmyshow.exception;

import java.util.List;

/**
 * Thrown when one or more requested seats are not available for booking.
 */
public class SeatNotAvailableException extends RuntimeException {

    private final List<Integer> unavailableSeatIds;

    public SeatNotAvailableException(String message) {
        super(message);
        this.unavailableSeatIds = List.of();
    }

    public SeatNotAvailableException(int seatId) {
        super("Seat not available: " + seatId);
        this.unavailableSeatIds = List.of(seatId);
    }

    public SeatNotAvailableException(List<Integer> unavailableSeatIds) {
        super("Seats not available: " + unavailableSeatIds);
        this.unavailableSeatIds = List.copyOf(unavailableSeatIds);
    }

    public List<Integer> getUnavailableSeatIds() {
        return unavailableSeatIds;
    }
}
