package com.bookmyshow.dao.jdbc;

import com.bookmyshow.dao.SeatDAO;
import com.bookmyshow.exception.DataAccessException;
import com.bookmyshow.exception.SeatNotAvailableException;
import com.bookmyshow.model.Seat;
import com.bookmyshow.model.enums.SeatStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JDBC implementation of {@link SeatDAO}.
 *
 * reserveSeats() is the database half of double-booking prevention: inside one
 * transaction it locks the requested rows (SELECT ... FOR UPDATE), and each UPDATE
 * only succeeds while the seat is still AVAILABLE. Either all seats are reserved or none.
 */
public class JdbcSeatDAO extends JdbcSupport implements SeatDAO {

    private static final String COLUMNS = "id, venue_id, event_id, seat_row, seat_number, seat_label, status";

    public JdbcSeatDAO(Database db) {
        super(db);
    }

    @Override
    public Seat create(Seat seat) {
        int id = insert("INSERT INTO seats (venue_id, event_id, seat_row, seat_number, seat_label, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                ps -> {
                    ps.setInt(1, seat.getVenueId());
                    ps.setInt(2, seat.getEventId());
                    ps.setString(3, seat.getSeatRow());
                    ps.setInt(4, seat.getSeatNumber());
                    ps.setString(5, seat.getSeatLabel());
                    ps.setString(6, seat.getStatus().name());
                });
        seat.setId(id);
        return seat;
    }

    @Override
    public Optional<Seat> findById(int id) {
        return queryOne("SELECT " + COLUMNS + " FROM seats WHERE id = ?",
                ps -> ps.setInt(1, id), JdbcSeatDAO::map);
    }

    @Override
    public List<Seat> findByEventId(int eventId) {
        return query("SELECT " + COLUMNS + " FROM seats WHERE event_id = ? ORDER BY id",
                ps -> ps.setInt(1, eventId), JdbcSeatDAO::map);
    }

    @Override
    public List<Seat> findByEventIdAndStatus(int eventId, SeatStatus status) {
        return query("SELECT " + COLUMNS + " FROM seats WHERE event_id = ? AND status = ? ORDER BY id",
                ps -> {
                    ps.setInt(1, eventId);
                    ps.setString(2, status.name());
                }, JdbcSeatDAO::map);
    }

    @Override
    public List<Seat> findByIds(List<Integer> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return List.of();
        }
        return query("SELECT " + COLUMNS + " FROM seats WHERE id IN (" + placeholders(seatIds.size()) + ") ORDER BY id",
                ps -> bindIds(ps, seatIds, 1), JdbcSeatDAO::map);
    }

    @Override
    public void reserveSeats(List<Integer> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return;
        }
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<Seat> locked = query(conn,
                        "SELECT " + COLUMNS + " FROM seats WHERE id IN (" + placeholders(seatIds.size()) + ") FOR UPDATE",
                        ps -> bindIds(ps, seatIds, 1), JdbcSeatDAO::map);
                Map<Integer, Seat> byId = locked.stream().collect(Collectors.toMap(Seat::getId, Function.identity()));
                List<Integer> unavailable = new ArrayList<>();
                for (Integer id : seatIds) {
                    Seat seat = byId.get(id);
                    if (seat == null || seat.getStatus() != SeatStatus.AVAILABLE) {
                        unavailable.add(id);
                    }
                }
                if (unavailable.isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE seats SET status = ? WHERE id = ? AND status = ?")) {
                        for (Integer id : seatIds) {
                            ps.setString(1, SeatStatus.RESERVED.name());
                            ps.setInt(2, id);
                            ps.setString(3, SeatStatus.AVAILABLE.name());
                            if (ps.executeUpdate() != 1) {
                                unavailable.add(id);
                            }
                        }
                    }
                }
                if (!unavailable.isEmpty()) {
                    conn.rollback();
                    throw new SeatNotAvailableException(unavailable);
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to reserve seats " + seatIds, e);
        }
    }

    @Override
    public void releaseSeats(List<Integer> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return;
        }
        update("UPDATE seats SET status = ? WHERE id IN (" + placeholders(seatIds.size()) + ")",
                ps -> {
                    ps.setString(1, SeatStatus.AVAILABLE.name());
                    bindIds(ps, seatIds, 2);
                });
    }

    @Override
    public Seat update(Seat seat) {
        update("UPDATE seats SET seat_row = ?, seat_number = ?, seat_label = ?, status = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, seat.getSeatRow());
                    ps.setInt(2, seat.getSeatNumber());
                    ps.setString(3, seat.getSeatLabel());
                    ps.setString(4, seat.getStatus().name());
                    ps.setInt(5, seat.getId());
                });
        return seat;
    }

    private static void bindIds(PreparedStatement ps, List<Integer> ids, int firstIndex) throws SQLException {
        for (int i = 0; i < ids.size(); i++) {
            ps.setInt(firstIndex + i, ids.get(i));
        }
    }

    private static Seat map(ResultSet rs) throws SQLException {
        Seat seat = new Seat(rs.getInt("id"), rs.getInt("venue_id"), rs.getInt("event_id"),
                rs.getString("seat_row"), rs.getInt("seat_number"));
        seat.setSeatLabel(rs.getString("seat_label"));
        seat.setStatus(SeatStatus.valueOf(rs.getString("status")));
        return seat;
    }
}
