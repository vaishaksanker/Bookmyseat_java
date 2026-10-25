package com.bookmyshow.service;

import com.bookmyshow.model.Ticket;

import java.util.List;

/**
 * Service interface for ticket generation and retrieval.
 *
 * A ticket is only generated after a booking has been CONFIRMED
 * (i.e., payment was successful). A ticket is never generated for
 * PENDING, FAILED, or CANCELLED bookings.
 */
public interface TicketService {

    /**
     * Generates a digital ticket for a confirmed booking.
     *
     * @param bookingId the booking for which to generate a ticket
     * @return the generated Ticket
     * @throws com.bookmyshow.exception.TicketGenerationException if the booking is not confirmed
     *         or a ticket has already been generated
     * @throws com.bookmyshow.exception.BookingException          if the booking does not exist
     */
    Ticket generateTicket(int bookingId);

    /**
     * Retrieves the ticket for a specific booking.
     *
     * @throws com.bookmyshow.exception.TicketGenerationException if no ticket exists for the booking
     */
    Ticket getTicketByBookingId(int bookingId);

    /**
     * Retrieves a ticket by its unique ticket number.
     *
     * @throws com.bookmyshow.exception.TicketGenerationException if not found
     */
    Ticket getTicketByNumber(String ticketNumber);

    /**
     * Returns all tickets for a user.
     */
    List<Ticket> getTicketsByUser(int userId);
}
