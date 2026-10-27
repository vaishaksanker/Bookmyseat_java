package com.bookmyshow.service;

import com.bookmyshow.dao.BookingDAO;
import com.bookmyshow.dao.EventDAO;
import com.bookmyshow.dao.SeatDAO;
import com.bookmyshow.exception.BookingException;
import com.bookmyshow.exception.EventNotFoundException;
import com.bookmyshow.exception.SeatNotAvailableException;
import com.bookmyshow.exception.ValidationException;
import com.bookmyshow.model.Booking;
import com.bookmyshow.model.BookingSeat;
import com.bookmyshow.model.Event;
import com.bookmyshow.model.Seat;
import com.bookmyshow.model.User;
import com.bookmyshow.model.enums.BookingStatus;
import com.bookmyshow.model.enums.SeatStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of BookingService.
 *
 * Double-booking prevention strategy:
 * 1. Service-layer check: verifies AVAILABLE status before proceeding.
 * 2. DAO-layer: reserveSeats() is expected to execute within a database transaction
 *    with SELECT FOR UPDATE or an equivalent lock, so that two concurrent requests
 *    for the same seat result in only one success.
 * 3. Database: UNIQUE(event_id, seat_id) constraint on booking_seats table acts as
 *    a final safety net.
 *
 * Total amount is always calculated by the backend — never trusted from the UI.
 */
public class BookingServiceImpl implements BookingService {

    private final BookingDAO bookingDAO;
    private final SeatDAO seatDAO;
    private final EventDAO eventDAO;

    public BookingServiceImpl(BookingDAO bookingDAO, SeatDAO seatDAO, EventDAO eventDAO) {
        this.bookingDAO = bookingDAO;
        this.seatDAO = seatDAO;
        this.eventDAO = eventDAO;
    }

    @Override
    public Booking createBooking(User user, int eventId, List<Integer> seatIds) {
        // --- Step 1: Validate user ---
        if (user == null) {
            throw new ValidationException("User must be logged in to make a booking");
        }

        // --- Step 2: Validate seat list ---
        validateSeatIds(seatIds);

        // --- Step 3: Validate event ---
        Event event = eventDAO.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.isBookable()) {
            throw new BookingException("Event is not available for booking. Status: " + event.getStatus()
                    + ", Available seats: " + event.getAvailableSeats());
        }

        // --- Step 4: Validate seats ---
        List<Seat> seats = seatDAO.findByIds(seatIds);

        validateSeatsExistAndBelongToEvent(seats, seatIds, eventId);
        validateSeatsAvailability(seats);

        // --- Step 5: Calculate total — backend owns the price ---
        BigDecimal totalAmount = event.getTicketPrice()
                .multiply(BigDecimal.valueOf(seatIds.size()));

        // --- Step 6: Create booking record ---
        Booking booking = new Booking();
        booking.setUserId(user.getId());
        booking.setEventId(eventId);
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingDAO.create(booking);

        // --- Step 7: Reserve seats (transactional at DAO level) ---
        // If reserveSeats throws SeatNotAvailableException, the booking is already
        // in PENDING status in the DB. We must mark it FAILED.
        try {
            seatDAO.reserveSeats(seatIds);
        } catch (SeatNotAvailableException e) {
            savedBooking.setStatus(BookingStatus.FAILED);
            bookingDAO.update(savedBooking);
            throw e;
        }

        // --- Step 8: Persist booking-seat join records ---
        List<BookingSeat> bookingSeats = buildBookingSeats(savedBooking.getId(), seats);
        bookingDAO.createBookingSeats(bookingSeats);
        bookingSeats.forEach(savedBooking::addBookingSeat);

        return savedBooking;
    }

    @Override
    public Booking confirmBooking(int bookingId) {
        Booking booking = getBookingById(bookingId);
        if (!booking.isPending()) {
            throw new BookingException(
                    "Cannot confirm booking in status: " + booking.getStatus()
                            + ". Booking must be PENDING.");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingDAO.update(booking);
    }

    @Override
    public Booking cancelBooking(int bookingId) {
        Booking booking = getBookingById(bookingId);

        if (BookingStatus.CANCELLED.equals(booking.getStatus())) {
            throw new BookingException("Booking is already cancelled");
        }
        // PENDING bookings hold reserved seats too; FAILED bookings already released theirs
        if (BookingStatus.CONFIRMED.equals(booking.getStatus())
                || BookingStatus.PENDING.equals(booking.getStatus())) {
            releaseBookingSeats(booking);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingDAO.update(booking);
    }

    @Override
    public Booking failBooking(int bookingId) {
        Booking booking = getBookingById(bookingId);
        if (!booking.isPending()) {
            throw new BookingException(
                    "Cannot fail booking in status: " + booking.getStatus()
                            + ". Booking must be PENDING.");
        }
        releaseBookingSeats(booking);

        booking.setStatus(BookingStatus.FAILED);
        return bookingDAO.update(booking);
    }

    private void releaseBookingSeats(Booking booking) {
        // Release seats back to AVAILABLE
        List<Integer> seatIds = booking.getBookingSeats()
                .stream()
                .map(BookingSeat::getSeatId)
                .collect(Collectors.toList());
        if (!seatIds.isEmpty()) {
            seatDAO.releaseSeats(seatIds);
        }
    }

    @Override
    public Booking getBookingById(int bookingId) {
        return bookingDAO.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found with id: " + bookingId));
    }

    @Override
    public List<Booking> getBookingsByUser(int userId) {
        return bookingDAO.findByUserId(userId);
    }

    // --- Private validation helpers ---

    private void validateSeatIds(List<Integer> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new ValidationException("seatIds", "Seat list must not be empty");
        }
        // Check for duplicates
        Set<Integer> uniqueIds = new HashSet<>(seatIds);
        if (uniqueIds.size() != seatIds.size()) {
            throw new ValidationException("seatIds", "Duplicate seat IDs are not allowed");
        }
        // Check for invalid IDs
        for (Integer id : seatIds) {
            if (id == null || id <= 0) {
                throw new ValidationException("seatIds", "All seat IDs must be positive integers");
            }
        }
    }

    private void validateSeatsExistAndBelongToEvent(List<Seat> seats, List<Integer> requestedIds,
                                                     int eventId) {
        if (seats.size() != requestedIds.size()) {
            Set<Integer> foundIds = seats.stream().map(Seat::getId).collect(Collectors.toSet());
            List<Integer> missing = requestedIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new ValidationException("seatIds", "Seat IDs not found: " + missing);
        }
        for (Seat seat : seats) {
            if (seat.getEventId() != eventId) {
                throw new ValidationException("seatIds",
                        "Seat " + seat.getId() + " does not belong to event " + eventId);
            }
        }
    }

    private void validateSeatsAvailability(List<Seat> seats) {
        List<Integer> unavailable = seats.stream()
                .filter(s -> !SeatStatus.AVAILABLE.equals(s.getStatus()))
                .map(Seat::getId)
                .collect(Collectors.toList());

        if (!unavailable.isEmpty()) {
            throw new SeatNotAvailableException(unavailable);
        }
    }

    private List<BookingSeat> buildBookingSeats(int bookingId, List<Seat> seats) {
        List<BookingSeat> result = new ArrayList<>();
        for (Seat seat : seats) {
            result.add(new BookingSeat(bookingId, seat.getId(), seat.getSeatLabel()));
        }
        return result;
    }
}
