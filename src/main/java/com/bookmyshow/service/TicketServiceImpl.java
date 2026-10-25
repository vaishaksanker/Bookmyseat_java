package com.bookmyshow.service;

import com.bookmyshow.dao.BookingDAO;
import com.bookmyshow.dao.EventDAO;
import com.bookmyshow.dao.TicketDAO;
import com.bookmyshow.dao.UserDAO;
import com.bookmyshow.exception.BookingException;
import com.bookmyshow.exception.TicketGenerationException;
import com.bookmyshow.model.Booking;
import com.bookmyshow.model.BookingSeat;
import com.bookmyshow.model.Event;
import com.bookmyshow.model.Ticket;
import com.bookmyshow.model.User;
import com.bookmyshow.model.enums.BookingStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of TicketService.
 *
 * A ticket can only be generated when:
 * 1. The booking exists
 * 2. The booking status is CONFIRMED (payment was successful)
 * 3. No ticket has already been generated for this booking
 *
 * Ticket contains: ticket number, booking ID, customer info, event info,
 * venue info, date/time, seat labels, total amount, payment status.
 */
public class TicketServiceImpl implements TicketService {

    private final TicketDAO ticketDAO;
    private final BookingDAO bookingDAO;
    private final EventDAO eventDAO;
    private final UserDAO userDAO;

    public TicketServiceImpl(TicketDAO ticketDAO,
                              BookingDAO bookingDAO,
                              EventDAO eventDAO,
                              UserDAO userDAO) {
        this.ticketDAO = ticketDAO;
        this.bookingDAO = bookingDAO;
        this.eventDAO = eventDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Ticket generateTicket(int bookingId) {
        // --- Step 1: Retrieve and validate booking ---
        Booking booking = bookingDAO.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found with id: " + bookingId));

        if (!BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            throw new TicketGenerationException(
                    "Cannot generate ticket for booking in status: " + booking.getStatus()
                            + ". Booking must be CONFIRMED (payment successful).");
        }

        // --- Step 2: Prevent duplicate ticket generation ---
        if (ticketDAO.findByBookingId(bookingId).isPresent()) {
            throw new TicketGenerationException(
                    "A ticket has already been generated for booking id: " + bookingId);
        }

        // --- Step 3: Load event and user ---
        Event event = eventDAO.findById(booking.getEventId())
                .orElseThrow(() -> new TicketGenerationException(
                        "Event not found for booking: " + bookingId));

        User user = userDAO.findById(booking.getUserId())
                .orElseThrow(() -> new TicketGenerationException(
                        "User not found for booking: " + bookingId));

        // --- Step 4: Collect seat labels ---
        List<String> seatLabels = booking.getBookingSeats()
                .stream()
                .map(BookingSeat::getSeatLabel)
                .collect(Collectors.toList());

        // --- Step 5: Build and persist ticket ---
        Ticket ticket = new Ticket(
                bookingId,
                user.getId(),
                event.getId(),
                user.getName(),
                event.getName(),
                event.getVenue() != null ? event.getVenue().getName() : "N/A",
                event.getEventDateTime(),
                seatLabels,
                booking.getTotalAmount()
        );

        return ticketDAO.create(ticket);
    }

    @Override
    public Ticket getTicketByBookingId(int bookingId) {
        return ticketDAO.findByBookingId(bookingId)
                .orElseThrow(() -> new TicketGenerationException(
                        "No ticket found for booking id: " + bookingId));
    }

    @Override
    public Ticket getTicketByNumber(String ticketNumber) {
        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new TicketGenerationException("Ticket number must not be empty");
        }
        return ticketDAO.findByTicketNumber(ticketNumber.trim())
                .orElseThrow(() -> new TicketGenerationException(
                        "No ticket found with number: " + ticketNumber));
    }

    @Override
    public List<Ticket> getTicketsByUser(int userId) {
        return ticketDAO.findByUserId(userId);
    }
}
