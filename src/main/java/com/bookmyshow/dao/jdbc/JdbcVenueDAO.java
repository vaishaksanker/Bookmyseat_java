package com.bookmyshow.dao.jdbc;

import com.bookmyshow.dao.VenueDAO;
import com.bookmyshow.model.Venue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link VenueDAO}.
 */
public class JdbcVenueDAO extends JdbcSupport implements VenueDAO {

    private static final String COLUMNS = "id, name, address, city, total_capacity";

    public JdbcVenueDAO(Database db) {
        super(db);
    }

    @Override
    public Venue create(Venue venue) {
        int id = insert("INSERT INTO venues (name, address, city, total_capacity) VALUES (?, ?, ?, ?)",
                ps -> {
                    ps.setString(1, venue.getName());
                    ps.setString(2, venue.getAddress());
                    ps.setString(3, venue.getCity());
                    ps.setInt(4, venue.getTotalCapacity());
                });
        venue.setId(id);
        return venue;
    }

    @Override
    public Optional<Venue> findById(int id) {
        return queryOne("SELECT " + COLUMNS + " FROM venues WHERE id = ?",
                ps -> ps.setInt(1, id), JdbcVenueDAO::map);
    }

    @Override
    public List<Venue> findAll() {
        return query("SELECT " + COLUMNS + " FROM venues ORDER BY id", ps -> { }, JdbcVenueDAO::map);
    }

    @Override
    public Venue update(Venue venue) {
        update("UPDATE venues SET name = ?, address = ?, city = ?, total_capacity = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, venue.getName());
                    ps.setString(2, venue.getAddress());
                    ps.setString(3, venue.getCity());
                    ps.setInt(4, venue.getTotalCapacity());
                    ps.setInt(5, venue.getId());
                });
        return venue;
    }

    static Venue map(ResultSet rs) throws SQLException {
        return new Venue(rs.getInt("id"), rs.getString("name"), rs.getString("address"),
                rs.getString("city"), rs.getInt("total_capacity"));
    }
}
