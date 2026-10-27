package com.bookmyshow.dao.jdbc;

import com.bookmyshow.dao.UserDAO;
import com.bookmyshow.model.User;
import com.bookmyshow.model.enums.Role;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * JDBC implementation of {@link UserDAO}.
 */
public class JdbcUserDAO extends JdbcSupport implements UserDAO {

    private static final String COLUMNS = "id, name, email, password_hash, role, active, created_at";

    public JdbcUserDAO(Database db) {
        super(db);
    }

    @Override
    public User create(User user) {
        int id = insert("INSERT INTO users (name, email, password_hash, role, active, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                ps -> {
                    ps.setString(1, user.getName());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, user.getPasswordHash());
                    ps.setString(4, user.getRole().name());
                    ps.setBoolean(5, user.isActive());
                    ps.setTimestamp(6, ts(user.getCreatedAt()));
                });
        user.setId(id);
        return user;
    }

    @Override
    public Optional<User> findById(int id) {
        return queryOne("SELECT " + COLUMNS + " FROM users WHERE id = ?",
                ps -> ps.setInt(1, id), JdbcUserDAO::map);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return queryOne("SELECT " + COLUMNS + " FROM users WHERE email = ?",
                ps -> ps.setString(1, email), JdbcUserDAO::map);
    }

    @Override
    public User update(User user) {
        update("UPDATE users SET name = ?, email = ?, password_hash = ?, role = ?, active = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, user.getName());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, user.getPasswordHash());
                    ps.setString(4, user.getRole().name());
                    ps.setBoolean(5, user.isActive());
                    ps.setInt(6, user.getId());
                });
        return user;
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    private static User map(ResultSet rs) throws SQLException {
        User user = new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"),
                rs.getString("password_hash"), Role.valueOf(rs.getString("role")));
        user.setActive(rs.getBoolean("active"));
        user.setCreatedAt(ldt(rs.getTimestamp("created_at")));
        return user;
    }
}
