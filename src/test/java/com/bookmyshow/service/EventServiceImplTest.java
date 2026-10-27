package com.bookmyshow.service;

import com.bookmyshow.dao.EventDAO;
import com.bookmyshow.dao.VenueDAO;
import com.bookmyshow.exception.EventNotFoundException;
import com.bookmyshow.exception.UnauthorizedException;
import com.bookmyshow.exception.ValidationException;
import com.bookmyshow.model.Event;
import com.bookmyshow.model.User;
import com.bookmyshow.model.Venue;
import com.bookmyshow.model.enums.EventStatus;
import com.bookmyshow.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventServiceImpl Unit Tests")
class EventServiceImplTest {

    @Mock
    private EventDAO eventDAO;
    @Mock
    private VenueDAO venueDAO;

    private EventServiceImpl eventService;

    private User organizer;
    private User customer;
    private User admin;
    private Venue venue;
    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventDAO, venueDAO);
        organizer = new User(1, "Org", "org@example.com", "hash", Role.ORGANIZER);
        customer  = new User(2, "Cust", "cust@example.com", "hash", Role.CUSTOMER);
        admin     = new User(3, "Admin", "admin@example.com", "hash", Role.ADMIN);
        venue     = new Venue(10, "Grand Hall", "123 Main St", "Mumbai", 500);
        futureDate = LocalDateTime.now().plusDays(30);
    }

    // --- createEvent ---

    @Test
    @DisplayName("createEvent: organizer with valid data creates event")
    void createEvent_validOrganizer_returnsEvent() {
        when(venueDAO.findById(10)).thenReturn(Optional.of(venue));
        Event saved = new Event(1, "Rock Concert", "A great show", venue,
                organizer.getId(), futureDate, new BigDecimal("500.00"), 200);
        saved.setStatus(EventStatus.PUBLISHED);
        when(eventDAO.create(any())).thenReturn(saved);

        Event result = eventService.createEvent(organizer, "Rock Concert", "A great show",
                10, futureDate, new BigDecimal("500.00"), 200);

        assertNotNull(result);
        assertEquals("Rock Concert", result.getName());
        verify(eventDAO).create(any());
    }

    @Test
    @DisplayName("createEvent: category and end time are stored on the event")
    void createEvent_withCategoryAndEndTime_storesBoth() {
        when(venueDAO.findById(10)).thenReturn(Optional.of(venue));
        when(eventDAO.create(any())).thenAnswer(inv -> inv.getArgument(0));
        LocalDateTime end = futureDate.plusHours(3);

        Event result = eventService.createEvent(organizer, "Hackathon", "Code all day", "HACKATHON",
                10, futureDate, end, new BigDecimal("0.00"), 100);

        assertEquals("HACKATHON", result.getCategory());
        assertEquals(end, result.getEndDateTime());
    }

    @Test
    @DisplayName("createEvent: end time before start throws ValidationException")
    void createEvent_endBeforeStart_throwsValidation() {
        assertThrows(ValidationException.class, () ->
                eventService.createEvent(organizer, "Hackathon", "Desc", "HACKATHON",
                        10, futureDate, futureDate.minusHours(1), new BigDecimal("0.00"), 100));
        verify(eventDAO, never()).create(any());
    }

    @Test
    @DisplayName("createEvent: customer role throws UnauthorizedException")
    void createEvent_customerRole_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () ->
                eventService.createEvent(customer, "Concert", "Desc",
                        10, futureDate, new BigDecimal("100.00"), 100));
        verify(eventDAO, never()).create(any());
    }

    @Test
    @DisplayName("createEvent: admin can create event")
    void createEvent_adminRole_succeeds() {
        when(venueDAO.findById(10)).thenReturn(Optional.of(venue));
        Event saved = new Event(2, "Admin Event", "", venue, admin.getId(),
                futureDate, BigDecimal.ZERO, 50);
        when(eventDAO.create(any())).thenReturn(saved);

        assertDoesNotThrow(() -> eventService.createEvent(admin, "Admin Event", "",
                10, futureDate, BigDecimal.ZERO, 50));
    }

    @Test
    @DisplayName("createEvent: empty name throws ValidationException")
    void createEvent_emptyName_throwsValidation() {
        assertThrows(ValidationException.class, () ->
                eventService.createEvent(organizer, "", "Desc",
                        10, futureDate, new BigDecimal("100.00"), 100));
    }

    @Test
    @DisplayName("createEvent: past date throws ValidationException")
    void createEvent_pastDate_throwsValidation() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        assertThrows(ValidationException.class, () ->
                eventService.createEvent(organizer, "Concert", "Desc",
                        10, pastDate, new BigDecimal("100.00"), 100));
    }

    @Test
    @DisplayName("createEvent: negative price throws ValidationException")
    void createEvent_negativePrice_throwsValidation() {
        assertThrows(ValidationException.class, () ->
                eventService.createEvent(organizer, "Concert", "Desc",
                        10, futureDate, new BigDecimal("-10.00"), 100));
    }

    @Test
    @DisplayName("createEvent: zero seats throws ValidationException")
    void createEvent_zeroSeats_throwsValidation() {
        assertThrows(ValidationException.class, () ->
                eventService.createEvent(organizer, "Concert", "Desc",
                        10, futureDate, new BigDecimal("100.00"), 0));
    }

    @Test
    @DisplayName("createEvent: unknown venue throws ValidationException")
    void createEvent_unknownVenue_throwsValidation() {
        when(venueDAO.findById(999)).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () ->
                eventService.createEvent(organizer, "Concert", "Desc",
                        999, futureDate, new BigDecimal("100.00"), 100));
    }

    // --- updateEvent ---

    @Test
    @DisplayName("updateEvent: owner can update their event")
    void updateEvent_owner_succeeds() {
        Event existing = new Event(1, "Old Name", "Desc", venue,
                organizer.getId(), futureDate, new BigDecimal("100.00"), 100);
        when(eventDAO.findById(1)).thenReturn(Optional.of(existing));
        when(eventDAO.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.updateEvent(organizer, 1, "New Name", null, null, null);
        assertEquals("New Name", result.getName());
    }

    @Test
    @DisplayName("updateEvent: non-owner organizer is rejected")
    void updateEvent_nonOwner_throwsUnauthorized() {
        User otherOrg = new User(99, "Other", "other@example.com", "hash", Role.ORGANIZER);
        Event existing = new Event(1, "Concert", "Desc", venue,
                organizer.getId(), futureDate, new BigDecimal("100.00"), 100);
        when(eventDAO.findById(1)).thenReturn(Optional.of(existing));

        assertThrows(UnauthorizedException.class, () ->
                eventService.updateEvent(otherOrg, 1, "New Name", null, null, null));
    }

    @Test
    @DisplayName("updateEvent: admin can update any event")
    void updateEvent_admin_succeeds() {
        Event existing = new Event(1, "Concert", "Desc", venue,
                organizer.getId(), futureDate, new BigDecimal("100.00"), 100);
        when(eventDAO.findById(1)).thenReturn(Optional.of(existing));
        when(eventDAO.update(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> eventService.updateEvent(admin, 1, "Updated", null, null, null));
    }

    @Test
    @DisplayName("updateEvent: event not found throws EventNotFoundException")
    void updateEvent_notFound_throwsEventNotFound() {
        when(eventDAO.findById(99)).thenReturn(Optional.empty());
        assertThrows(EventNotFoundException.class, () ->
                eventService.updateEvent(organizer, 99, "Name", null, null, null));
    }

    // --- getEventById ---

    @Test
    @DisplayName("getEventById: existing event is returned")
    void getEventById_found_returnsEvent() {
        Event event = new Event(5, "Festival", "", venue, 1,
                futureDate, new BigDecimal("200.00"), 300);
        when(eventDAO.findById(5)).thenReturn(Optional.of(event));

        Event result = eventService.getEventById(5);
        assertEquals(5, result.getId());
    }

    @Test
    @DisplayName("getEventById: not found throws EventNotFoundException")
    void getEventById_notFound_throwsException() {
        when(eventDAO.findById(99)).thenReturn(Optional.empty());
        assertThrows(EventNotFoundException.class, () -> eventService.getEventById(99));
    }

    // --- getAllEvents / getPublishedEvents ---

    @Test
    @DisplayName("getAllEvents: delegates to DAO")
    void getAllEvents_returnsList() {
        when(eventDAO.findAll()).thenReturn(List.of());
        List<Event> result = eventService.getAllEvents();
        assertNotNull(result);
        verify(eventDAO).findAll();
    }

    @Test
    @DisplayName("getPublishedEvents: delegates to DAO with PUBLISHED status")
    void getPublishedEvents_delegatesToDAO() {
        when(eventDAO.findByStatus(EventStatus.PUBLISHED)).thenReturn(List.of());
        eventService.getPublishedEvents();
        verify(eventDAO).findByStatus(EventStatus.PUBLISHED);
    }

    // --- searchEvents ---

    @Test
    @DisplayName("searchEvents: valid keyword returns results")
    void searchEvents_validKeyword_returnsList() {
        Event e = new Event(1, "Rock Concert", "", venue, 1,
                futureDate, new BigDecimal("100.00"), 100);
        when(eventDAO.searchByName("rock")).thenReturn(List.of(e));

        List<Event> result = eventService.searchEvents("rock");
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchEvents: empty keyword throws ValidationException")
    void searchEvents_emptyKeyword_throwsValidation() {
        assertThrows(ValidationException.class, () -> eventService.searchEvents(""));
        assertThrows(ValidationException.class, () -> eventService.searchEvents(null));
    }
}
