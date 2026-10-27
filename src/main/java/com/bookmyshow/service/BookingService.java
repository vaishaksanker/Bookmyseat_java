package com.bookmyshow.service;

import com.bookmyshow.model.Booking;
import com.bookmyshow.model.User;

import java.util.List;

/**
 * Service interface for the seat booking business logic.
 *
 * The booking flow is:
 * validate user → validate event → validate seats → check availability
 * → calculate total → create booking → reserve seats → commit → return booking
 */
public interface BookingService {

    /**
     * Creates a booking for the specified seats at an event.
     *
     * Seat availability is verified before booking. Double-booking is prevented
     * at both the service layer (checks) and the database layer (UNIQUE constraint).
     *
     * @param user     the authenticated user making the booking
     * @param eventId  the event to book
     * @param seatIds  the list of seat IDs to book (must not be empty, no duplicates)
     * @return the created Booking with PENDING status (before payment)
     * @throws com.bookmyshow.exception.ValidationException        if input is invalid
     * @throws com.bookmyshow.exception.EventNotFoundException     if the event does not exist
     * @throws com.bookmyshow.exception.SeatNotAvailableException  if any seat is not available
     * @throws com.bookmyshow.exception.BookingException           if the booking fails
     */
    Booking createBooking(User user, int eventId, List<Integer> seatIds);

    /**
     * Confirms a booking after successful payment.
     *
     * @throws com.bookmyshow.exception.BookingException if the booking cannot be confirmed
     */
    Booking confirmBooking(int bookingId);

    /**
     * Cancels a booking and releases its seats back to AVAILABLE.
     *
     * @throws com.bookmyshow.exception.BookingException if the booking cannot be cancelled
     */
    Booking cancelBooking(int bookingId);

    /**
     * Marks a PENDING booking as FAILED (e.g. payment declined) and releases
     * its reserved seats so other users can book them.
     *
     * @throws com.bookmyshow.exception.BookingException if the booking is not PENDING
     */
    Booking failBooking(int bookingId);

    /**
     * Retrieves a booking by ID.
     *
     * @throws com.bookmyshow.exception.BookingException if not found
     */
    Booking getBookingById(int bookingId);

    /**
     * Returns all bookings made by a user.
     */
    List<Booking> getBookingsByUser(int userId);
}
