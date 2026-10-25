package com.bookmyshow.dao;

import com.bookmyshow.model.User;

import java.util.Optional;

/**
 * DAO interface for User persistence.
 * Implemented by Member 1 using JDBC/PostgreSQL.
 */
public interface UserDAO {

    /**
     * Persists a new user and returns it with the generated ID.
     */
    User create(User user);

    /**
     * Finds a user by their primary key.
     */
    Optional<User> findById(int id);

    /**
     * Finds a user by their email address (used for login lookups).
     */
    Optional<User> findByEmail(String email);

    /**
     * Updates an existing user's details.
     */
    User update(User user);

    /**
     * Checks whether an email is already registered.
     */
    boolean existsByEmail(String email);
}
