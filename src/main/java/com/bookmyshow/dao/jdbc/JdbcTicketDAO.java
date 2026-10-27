package com.bookmyshow.dao.jdbc;

import com.bookmyshow.dao.TicketDAO;
import com.bookmyshow.model.Ticket;
import com.bookmyshow.model.enums.PaymentStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link TicketDAO}. Seat labels are stored as a comma-separated list.
 */
public class JdbcTicketDAO extends JdbcSupport implements TicketDAO {

    private static final String COLUMNS = "id, ticket_number, booking_id, user_id, event_id, customer_name, "
            + "event_name, venue_name, event_date_time, seat_labels, total_amount, payment_status, generated_at";

    public JdbcTicketDAO(Database db) {
        super(db);
    }

    @Override
    public Ticket create(Ticket ticket) {
        int id = insert("INSERT INTO tickets (ticket_number, booking_id, user_id, event_id, customer_name, "
                        + "event_name, venue_name, event_date_time, seat_labels, total_amount, payment_status, "
                        + "generated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                ps -> {
                    ps.setString(1, ticket.getTicketNumber());
                    ps.setInt(2, ticket.getBookingId());
                    ps.setInt(3, ticket.getUserId());
                    ps.setInt(4, ticket.getEventId());
                    ps.setString(5, ticket.getCustomerName());
                    ps.setString(6, ticket.getEventName());
                    ps.setString(7, ticket.getVenueName());
                    ps.setTimestamp(8, ts(ticket.getEventDateTime()));
                    ps.setString(9, String.join(",", ticket.getSeatLabels()));
                    ps.setBigDecimal(10, ticket.getTotalAmount());
                    ps.setString(11, ticket.getPaymentStatus() != null ? ticket.getPaymentStatus().name() : null);
                    ps.setTimestamp(12, ts(ticket.getGeneratedAt()));
                });
        ticket.setId(id);
        return ticket;
    }

    @Override
    public Optional<Ticket> findById(int id) {
        return queryOne("SELECT " + COLUMNS + " FROM tickets WHERE id = ?",
                ps -> ps.setInt(1, id), JdbcTicketDAO::map);
    }

    @Override
    public Optional<Ticket> findByBookingId(int bookingId) {
        return queryOne("SELECT " + COLUMNS + " FROM tickets WHERE booking_id = ?",
                ps -> ps.setInt(1, bookingId), JdbcTicketDAO::map);
    }

    @Override
    public Optional<Ticket> findByTicketNumber(String ticketNumber) {
        return queryOne("SELECT " + COLUMNS + " FROM tickets WHERE ticket_number = ?",
                ps -> ps.setString(1, ticketNumber), JdbcTicketDAO::map);
    }

    @Override
    public List<Ticket> findByUserId(int userId) {
        return query("SELECT " + COLUMNS + " FROM tickets WHERE user_id = ? ORDER BY generated_at DESC, id DESC",
                ps -> ps.setInt(1, userId), JdbcTicketDAO::map);
    }

    private static Ticket map(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getInt("id"));
        ticket.setTicketNumber(rs.getString("ticket_number"));
        ticket.setBookingId(rs.getInt("booking_id"));
        ticket.setUserId(rs.getInt("user_id"));
        ticket.setEventId(rs.getInt("event_id"));
        ticket.setCustomerName(rs.getString("customer_name"));
        ticket.setEventName(rs.getString("event_name"));
        ticket.setVenueName(rs.getString("venue_name"));
        ticket.setEventDateTime(ldt(rs.getTimestamp("event_date_time")));
        String labels = rs.getString("seat_labels");
        ticket.setSeatLabels(labels == null || labels.isEmpty() ? List.of() : Arrays.asList(labels.split(",")));
        ticket.setTotalAmount(rs.getBigDecimal("total_amount"));
        String status = rs.getString("payment_status");
        ticket.setPaymentStatus(status != null ? PaymentStatus.valueOf(status) : null);
        ticket.setGeneratedAt(ldt(rs.getTimestamp("generated_at")));
        return ticket;
    }
}
