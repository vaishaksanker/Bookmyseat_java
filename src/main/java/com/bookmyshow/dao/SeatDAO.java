package com.bookmyshow.dao;

import com.bookmyshow.model.Seat;
import com.bookmyshow.model.enums.SeatStatus;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Seat persistence.
 * Implemented by Member 1 using JDBC/PostgreSQL.
 */
public interface SeatDAO {

    Seat create(Seat seat);

    Optional<Seat> findById(int id);

    List<Seat> findByEventId(int eventId);

    List<Seat> findByEventIdAndStatus(int eventId, SeatStatus status);

    /**
     * Fetches specific seats by IDs for an event — used in booking validation.
     */
    List<Seat> findByIds(List<Integer> seatIds);

    /**
     * Atomically marks seats as BOOKED within an active database transaction.
     * The caller is responsible for transaction management.
     * Throws SeatNotAvailableException if any seat is no longer AVAILABLE.
     */
    void reserveSeats(List<Integer> seatIds);

    /**
     * Releases previously reserved seats back to AVAILABLE (e.g., on booking failure).
     */
    void releaseSeats(List<Integer> seatIds);

    Seat update(Seat seat);
}
