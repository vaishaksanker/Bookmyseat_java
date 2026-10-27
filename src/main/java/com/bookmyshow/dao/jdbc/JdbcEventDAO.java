package com.bookmyshow.dao.jdbc;

import com.bookmyshow.dao.EventDAO;
import com.bookmyshow.exception.DataAccessException;
import com.bookmyshow.model.Event;
import com.bookmyshow.model.Venue;
import com.bookmyshow.model.enums.EventStatus;
import com.bookmyshow.model.enums.SeatStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link EventDAO}.
 *
 * - create() also generates the event's seat map (rows of 10: A1..A10, B1..B10, ...)
 *   in the same transaction, so every event is bookable as soon as it exists.
 * - availableSeats is computed from the seats table on every read, so it can never
 *   drift from the real seat statuses.
 * - delete() is a soft delete (status CANCELLED): bookings and tickets keep their history.
 */
public class JdbcEventDAO extends JdbcSupport implements EventDAO {

    static final int SEATS_PER_ROW = 10;

    private static final String SELECT =
            "SELECT e.id, e.name, e.description, e.category, e.organizer_id, e.event_date_time, "
            + "e.end_date_time, e.ticket_price, e.total_seats, e.status, e.created_at, "
            + "v.id AS venue_id, v.name AS venue_name, v.address AS venue_address, "
            + "v.city AS venue_city, v.total_capacity AS venue_capacity, "
            + "(SELECT COUNT(*) FROM seats s WHERE s.event_id = e.id AND s.status = 'AVAILABLE') AS available_seats "
            + "FROM events e JOIN venues v ON v.id = e.venue_id ";

    public JdbcEventDAO(Database db) {
        super(db);
    }

    @Override
    public Event create(Event event) {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int id = insert(conn, "INSERT INTO events (name, description, category, venue_id, organizer_id, "
                                + "event_date_time, end_date_time, ticket_price, total_seats, status, created_at) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        ps -> {
                            ps.setString(1, event.getName());
                            ps.setString(2, event.getDescription());
                            ps.setString(3, event.getCategory());
                            ps.setInt(4, event.getVenue().getId());
                            ps.setInt(5, event.getOrganizerId());
                            ps.setTimestamp(6, ts(event.getEventDateTime()));
                            ps.setTimestamp(7, ts(event.getEndDateTime()));
                            ps.setBigDecimal(8, event.getTicketPrice());
                            ps.setInt(9, event.getTotalSeats());
                            ps.setString(10, event.getStatus().name());
                            ps.setTimestamp(11, ts(event.getCreatedAt()));
                        });
                insertSeatMap(conn, id, event.getVenue().getId(), event.getTotalSeats());
                conn.commit();
                event.setId(id);
                event.setAvailableSeats(event.getTotalSeats());
                return event;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create event: " + event.getName(), e);
        }
    }

    @Override
    public Optional<Event> findById(int id) {
        return queryOne(SELECT + "WHERE e.id = ?", ps -> ps.setInt(1, id), JdbcEventDAO::map);
    }

    @Override
    public List<Event> findAll() {
        return query(SELECT + "ORDER BY e.event_date_time, e.id", ps -> { }, JdbcEventDAO::map);
    }

    @Override
    public List<Event> findByStatus(EventStatus status) {
        return query(SELECT + "WHERE e.status = ? ORDER BY e.event_date_time, e.id",
                ps -> ps.setString(1, status.name()), JdbcEventDAO::map);
    }

    @Override
    public List<Event> findByOrganizer(int organizerId) {
        return query(SELECT + "WHERE e.organizer_id = ? ORDER BY e.event_date_time, e.id",
                ps -> ps.setInt(1, organizerId), JdbcEventDAO::map);
    }

    @Override
    public List<Event> searchByName(String keyword) {
        return query(SELECT + "WHERE LOWER(e.name) LIKE ? ORDER BY e.event_date_time, e.id",
                ps -> ps.setString(1, "%" + keyword.toLowerCase() + "%"), JdbcEventDAO::map);
    }

    @Override
    public Event update(Event event) {
        update("UPDATE events SET name = ?, description = ?, category = ?, venue_id = ?, "
                        + "event_date_time = ?, end_date_time = ?, ticket_price = ?, status = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, event.getName());
                    ps.setString(2, event.getDescription());
                    ps.setString(3, event.getCategory());
                    ps.setInt(4, event.getVenue().getId());
                    ps.setTimestamp(5, ts(event.getEventDateTime()));
                    ps.setTimestamp(6, ts(event.getEndDateTime()));
                    ps.setBigDecimal(7, event.getTicketPrice());
                    ps.setString(8, event.getStatus().name());
                    ps.setInt(9, event.getId());
                });
        return findById(event.getId()).orElse(event);
    }

    @Override
    public void delete(int id) {
        update("UPDATE events SET status = ? WHERE id = ?", ps -> {
            ps.setString(1, EventStatus.CANCELLED.name());
            ps.setInt(2, id);
        });
    }

    private void insertSeatMap(Connection conn, int eventId, int venueId, int totalSeats) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO seats (venue_id, event_id, seat_row, seat_number, seat_label, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)")) {
            for (int i = 0; i < totalSeats; i++) {
                String row = rowLabel(i / SEATS_PER_ROW);
                int number = i % SEATS_PER_ROW + 1;
                ps.setInt(1, venueId);
                ps.setInt(2, eventId);
                ps.setString(3, row);
                ps.setInt(4, number);
                ps.setString(5, row + number);
                ps.setString(6, SeatStatus.AVAILABLE.name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** 0 → A, 25 → Z, 26 → AA, 27 → AB ... */
    static String rowLabel(int index) {
        StringBuilder label = new StringBuilder();
        int n = index;
        do {
            label.insert(0, (char) ('A' + n % 26));
            n = n / 26 - 1;
        } while (n >= 0);
        return label.toString();
    }

    private static Event map(ResultSet rs) throws SQLException {
        Venue venue = new Venue(rs.getInt("venue_id"), rs.getString("venue_name"),
                rs.getString("venue_address"), rs.getString("venue_city"), rs.getInt("venue_capacity"));
        Event event = new Event(rs.getInt("id"), rs.getString("name"), rs.getString("description"), venue,
                rs.getInt("organizer_id"), ldt(rs.getTimestamp("event_date_time")),
                rs.getBigDecimal("ticket_price"), rs.getInt("total_seats"));
        event.setCategory(rs.getString("category"));
        event.setEndDateTime(ldt(rs.getTimestamp("end_date_time")));
        event.setAvailableSeats(rs.getInt("available_seats"));
        event.setStatus(EventStatus.valueOf(rs.getString("status")));
        event.setCreatedAt(ldt(rs.getTimestamp("created_at")));
        return event;
    }
}
