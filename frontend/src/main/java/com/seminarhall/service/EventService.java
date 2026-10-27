package com.seminarhall.service;

import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;

import java.time.LocalDate;
import java.util.List;

public interface EventService {
    List<Event> getAllEvents() throws ServiceException;
    List<Event> getUpcomingEvents() throws ServiceException;
    List<Event> getFeaturedEvents() throws ServiceException;
    List<Event> getEventsByCategory(EventCategory category) throws ServiceException;
    List<Event> searchEvents(String query, EventCategory category, String sortBy) throws ServiceException;
    Event getEventById(String eventId) throws ServiceException;
    Event createEvent(Event event) throws ServiceException;
    Event updateEvent(Event event) throws ServiceException;
    void deleteEvent(String eventId) throws ServiceException;
    void cancelEvent(String eventId) throws ServiceException;
}
