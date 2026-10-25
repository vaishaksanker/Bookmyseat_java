package com.bookmyshow.dao;

import com.bookmyshow.model.Booking;
import com.bookmyshow.model.BookingSeat;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Booking persistence.
 * Implemented by Member 1 using JDBC/PostgreSQL.
 */
public interface BookingDAO {

    /**
     * Persists a new booking and returns it with the generated ID.
     */
    Booking create(Booking booking);

    Optional<Booking> findById(int id);

    List<Booking> findByUserId(int userId);

    List<Booking> findByEventId(int eventId);

    Booking update(Booking booking);

    /**
     * Inserts all booking-seat join records in a single batch operation.
     */
    void createBookingSeats(List<BookingSeat> bookingSeats);
}
