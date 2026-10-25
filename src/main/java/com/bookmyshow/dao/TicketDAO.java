package com.bookmyshow.dao;

import com.bookmyshow.model.Ticket;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Ticket persistence.
 * Implemented by Member 1 using JDBC/PostgreSQL.
 */
public interface TicketDAO {

    Ticket create(Ticket ticket);

    Optional<Ticket> findById(int id);

    Optional<Ticket> findByBookingId(int bookingId);

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByUserId(int userId);
}
