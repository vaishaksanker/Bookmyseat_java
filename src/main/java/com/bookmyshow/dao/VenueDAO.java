package com.bookmyshow.dao;

import com.bookmyshow.model.Venue;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Venue persistence.
 * Implemented by Member 1 using JDBC/PostgreSQL.
 */
public interface VenueDAO {

    Venue create(Venue venue);

    Optional<Venue> findById(int id);

    List<Venue> findAll();

    Venue update(Venue venue);
}
