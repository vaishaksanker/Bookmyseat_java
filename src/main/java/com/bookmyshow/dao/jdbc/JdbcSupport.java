package com.bookmyshow.dao.jdbc;

import com.bookmyshow.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Shared plumbing for the JDBC DAOs: one connection per call, SQLExceptions
 * wrapped in DataAccessException, and null-safe timestamp conversion.
 */
abstract class JdbcSupport {

    @FunctionalInterface
    interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected final Database db;

    protected JdbcSupport(Database db) {
        this.db = db;
    }

    protected <T> List<T> query(String sql, Binder binder, RowMapper<T> mapper) {
        try (Connection conn = db.getConnection()) {
            return query(conn, sql, binder, mapper);
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sql, e);
        }
    }

    protected <T> List<T> query(Connection conn, String sql, Binder binder, RowMapper<T> mapper)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
                return results;
            }
        }
    }

    protected <T> Optional<T> queryOne(String sql, Binder binder, RowMapper<T> mapper) {
        List<T> results = query(sql, binder, mapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    protected int update(String sql, Binder binder) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Update failed: " + sql, e);
        }
    }

    /** Runs an INSERT and returns the generated primary key. */
    protected int insert(String sql, Binder binder) {
        try (Connection conn = db.getConnection()) {
            return insert(conn, sql, binder);
        } catch (SQLException e) {
            throw new DataAccessException("Insert failed: " + sql, e);
        }
    }

    protected int insert(Connection conn, String sql, Binder binder) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            binder.bind(ps);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated key returned for: " + sql);
                }
                return keys.getInt(1);
            }
        }
    }

    /** "?, ?, ?" for an IN clause with n values. */
    protected static String placeholders(int n) {
        return String.join(", ", Collections.nCopies(n, "?"));
    }

    protected static Timestamp ts(LocalDateTime value) {
        return value != null ? Timestamp.valueOf(value) : null;
    }

    protected static LocalDateTime ldt(Timestamp value) {
        return value != null ? value.toLocalDateTime() : null;
    }
}
