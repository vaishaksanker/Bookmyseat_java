package com.bookmyshow.dao.jdbc;

import com.bookmyshow.dao.BookingDAO;
import com.bookmyshow.exception.DataAccessException;
import com.bookmyshow.model.Booking;
import com.bookmyshow.model.BookingSeat;
import com.bookmyshow.model.enums.BookingStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link BookingDAO}. Bookings are always loaded with their seats.
 */
public class JdbcBookingDAO extends JdbcSupport implements BookingDAO {

    private static final String COLUMNS = "id, user_id, event_id, status, total_amount, booking_time";

    public JdbcBookingDAO(Database db) {
        super(db);
    }

    @Override
    public Booking create(Booking booking) {
        int id = insert("INSERT INTO bookings (user_id, event_id, status, total_amount, booking_time) "
                        + "VALUES (?, ?, ?, ?, ?)",
                ps -> {
                    ps.setInt(1, booking.getUserId());
                    ps.setInt(2, booking.getEventId());
                    ps.setString(3, booking.getStatus().name());
                    ps.setBigDecimal(4, booking.getTotalAmount());
                    ps.setTimestamp(5, ts(booking.getBookingTime()));
                });
        booking.setId(id);
        return booking;
    }

    @Override
    public Optional<Booking> findById(int id) {
        return loadSeats(query("SELECT " + COLUMNS + " FROM bookings WHERE id = ?",
                ps -> ps.setInt(1, id), JdbcBookingDAO::map)).stream().findFirst();
    }

    @Override
    public List<Booking> findByUserId(int userId) {
        return loadSeats(query("SELECT " + COLUMNS + " FROM bookings WHERE user_id = ? ORDER BY booking_time DESC, id DESC",
                ps -> ps.setInt(1, userId), JdbcBookingDAO::map));
    }

    @Override
    public List<Booking> findByEventId(int eventId) {
        return loadSeats(query("SELECT " + COLUMNS + " FROM bookings WHERE event_id = ? ORDER BY booking_time DESC, id DESC",
                ps -> ps.setInt(1, eventId), JdbcBookingDAO::map));
    }

    @Override
    public Booking update(Booking booking) {
        update("UPDATE bookings SET status = ?, total_amount = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, booking.getStatus().name());
                    ps.setBigDecimal(2, booking.getTotalAmount());
                    ps.setInt(3, booking.getId());
                });
        return booking;
    }

    @Override
    public void createBookingSeats(List<BookingSeat> bookingSeats) {
        if (bookingSeats == null || bookingSeats.isEmpty()) {
            return;
        }
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO booking_seats (booking_id, seat_id, seat_label) VALUES (?, ?, ?)")) {
            for (BookingSeat seat : bookingSeats) {
                ps.setInt(1, seat.getBookingId());
                ps.setInt(2, seat.getSeatId());
                ps.setString(3, seat.getSeatLabel());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save booking seats", e);
        }
    }

    private List<Booking> loadSeats(List<Booking> bookings) {
        for (Booking booking : bookings) {
            booking.setBookingSeats(query(
                    "SELECT id, booking_id, seat_id, seat_label FROM booking_seats WHERE booking_id = ? ORDER BY id",
                    ps -> ps.setInt(1, booking.getId()),
                    rs -> new BookingSeat(rs.getInt("id"), rs.getInt("booking_id"),
                            rs.getInt("seat_id"), rs.getString("seat_label"))));
        }
        return bookings;
    }

    private static Booking map(ResultSet rs) throws SQLException {
        Booking booking = new Booking(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("event_id"),
                rs.getBigDecimal("total_amount"));
        booking.setStatus(BookingStatus.valueOf(rs.getString("status")));
        booking.setBookingTime(ldt(rs.getTimestamp("booking_time")));
        return booking;
    }
}
