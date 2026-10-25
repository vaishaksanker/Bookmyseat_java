package com.bookmyshow.dao;

import com.bookmyshow.model.Event;
import com.bookmyshow.model.enums.EventStatus;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Event persistence.
 * Implemented by Member 1 using JDBC/PostgreSQL.
 */
public interface EventDAO {

    Event create(Event event);

    Optional<Event> findById(int id);

    List<Event> findAll();

    List<Event> findByStatus(EventStatus status);

    List<Event> findByOrganizer(int organizerId);

    /**
     * Searches events by name (case-insensitive partial match).
     */
    List<Event> searchByName(String keyword);

    Event update(Event event);

    void delete(int id);
}
