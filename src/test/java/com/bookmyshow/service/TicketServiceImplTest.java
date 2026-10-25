package com.bookmyshow.service;

import com.bookmyshow.dao.BookingDAO;
import com.bookmyshow.dao.EventDAO;
import com.bookmyshow.dao.TicketDAO;
import com.bookmyshow.dao.UserDAO;
import com.bookmyshow.exception.BookingException;
import com.bookmyshow.exception.TicketGenerationException;
import com.bookmyshow.model.Booking;
import com.bookmyshow.model.BookingSeat;
import com.bookmyshow.model.Event;
import com.bookmyshow.model.Ticket;
import com.bookmyshow.model.User;
import com.bookmyshow.model.Venue;
import com.bookmyshow.model.enums.BookingStatus;
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
@DisplayName("TicketServiceImpl Unit Tests")
class TicketServiceImplTest {

    @Mock private TicketDAO ticketDAO;
    @Mock private BookingDAO bookingDAO;
    @Mock private EventDAO eventDAO;
    @Mock private UserDAO userDAO;

    private TicketServiceImpl ticketService;

    private Booking confirmedBooking;
    private Event event;
    private User customer;
    private Venue venue;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(ticketDAO, bookingDAO, eventDAO, userDAO);

        customer = new User(1, "Alice", "alice@example.com", "hash", Role.CUSTOMER);
        venue = new Venue(10, "Grand Hall", "Addr", "Mumbai", 500);

        event = new Event(5, "Rock Concert", "Desc", venue, 99,
                LocalDateTime.now().plusDays(10), new BigDecimal("200.00"), 100);
        event.setStatus(EventStatus.PUBLISHED);

        confirmedBooking = new Booking(1, customer.getId(), event.getId(), new BigDecimal("400.00"));
        confirmedBooking.setStatus(BookingStatus.CONFIRMED);
        confirmedBooking.addBookingSeat(new BookingSeat(1, 101, "A1"));
        confirmedBooking.addBookingSeat(new BookingSeat(1, 102, "A2"));
    }

    // =========================================================
    // generateTicket — happy path
    // =========================================================

    @Test
    @DisplayName("generateTicket: confirmed booking produces a valid ticket")
    void generateTicket_confirmedBooking_returnsTicket() {
        when(bookingDAO.findById(1)).thenReturn(Optional.of(confirmedBooking));
        when(ticketDAO.findByBookingId(1)).thenReturn(Optional.empty());
        when(eventDAO.findById(5)).thenReturn(Optional.of(event));
        when(userDAO.findById(1)).thenReturn(Optional.of(customer));

        Ticket savedTicket = new Ticket(1, 1, 5, "Alice", "Rock Concert",
                "Grand Hall", event.getEventDateTime(),
                List.of("A1", "A2"), new BigDecimal("400.00"));
        when(ticketDAO.create(any())).thenReturn(savedTicket);

        Ticket result = ticketService.generateTicket(1);

        assertNotNull(result);
        assertEquals("Rock Concert", result.getEventName());
        assertEquals("Alice", result.getCustomerName());
        assertEquals(List.of("A1", "A2"), result.getSeatLabels());
        verify(ticketDAO).create(any());
    }

    @Test
    @DisplayName("generateTicket: ticket contains correct booking ID")
    void generateTicket_containsCorrectBookingId() {
        when(bookingDAO.findById(1)).thenReturn(Optional.of(confirmedBooking));
        when(ticketDAO.findByBookingId(1)).thenReturn(Optional.empty());
        when(eventDAO.findById(5)).thenReturn(Optional.of(event));
        when(userDAO.findById(1)).thenReturn(Optional.of(customer));

        Ticket savedTicket = new Ticket(1, 1, 5, "Alice", "Rock Concert",
                "Grand Hall", event.getEventDateTime(),
                List.of("A1", "A2"), new BigDecimal("400.00"));
        when(ticketDAO.create(any())).thenReturn(savedTicket);

        Ticket result = ticketService.generateTicket(1);
        assertEquals(1, result.getBookingId());
    }

    // =========================================================
    // generateTicket — failure cases
    // =========================================================

    @Test
    @DisplayName("generateTicket: PENDING booking throws TicketGenerationException")
    void generateTicket_pendingBooking_throwsException() {
        Booking pendingBooking = new Booking(2, 1, 5, new BigDecimal("200.00"));
        // status = PENDING by default
        when(bookingDAO.findById(2)).thenReturn(Optional.of(pendingBooking));

        assertThrows(TicketGenerationException.class, () ->
                ticketService.generateTicket(2));
        verify(ticketDAO, never()).create(any());
    }

    @Test
    @DisplayName("generateTicket: FAILED booking throws TicketGenerationException")
    void generateTicket_failedBooking_throwsException() {
        Booking failedBooking = new Booking(3, 1, 5, new BigDecimal("200.00"));
        failedBooking.setStatus(BookingStatus.FAILED);
        when(bookingDAO.findById(3)).thenReturn(Optional.of(failedBooking));

        assertThrows(TicketGenerationException.class, () ->
                ticketService.generateTicket(3));
    }

    @Test
    @DisplayName("generateTicket: CANCELLED booking throws TicketGenerationException")
    void generateTicket_cancelledBooking_throwsException() {
        Booking cancelledBooking = new Booking(4, 1, 5, new BigDecimal("200.00"));
        cancelledBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingDAO.findById(4)).thenReturn(Optional.of(cancelledBooking));

        assertThrows(TicketGenerationException.class, () ->
                ticketService.generateTicket(4));
    }

    @Test
    @DisplayName("generateTicket: booking not found throws BookingException")
    void generateTicket_bookingNotFound_throwsBookingException() {
        when(bookingDAO.findById(99)).thenReturn(Optional.empty());

        assertThrows(BookingException.class, () ->
                ticketService.generateTicket(99));
    }

    @Test
    @DisplayName("generateTicket: duplicate ticket generation throws TicketGenerationException")
    void generateTicket_duplicateAttempt_throwsException() {
        when(bookingDAO.findById(1)).thenReturn(Optional.of(confirmedBooking));
        // A ticket already exists for this booking
        Ticket existing = new Ticket();
        existing.setBookingId(1);
        when(ticketDAO.findByBookingId(1)).thenReturn(Optional.of(existing));

        assertThrows(TicketGenerationException.class, () ->
                ticketService.generateTicket(1));
        verify(ticketDAO, never()).create(any());
    }

    // =========================================================
    // getTicketByBookingId
    // =========================================================

    @Test
    @DisplayName("getTicketByBookingId: existing ticket is returned")
    void getTicketByBookingId_found_returnsTicket() {
        Ticket ticket = new Ticket();
        ticket.setBookingId(1);
        when(ticketDAO.findByBookingId(1)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicketByBookingId(1);
        assertNotNull(result);
    }

    @Test
    @DisplayName("getTicketByBookingId: not found throws TicketGenerationException")
    void getTicketByBookingId_notFound_throwsException() {
        when(ticketDAO.findByBookingId(99)).thenReturn(Optional.empty());
        assertThrows(TicketGenerationException.class, () ->
                ticketService.getTicketByBookingId(99));
    }

    // =========================================================
    // getTicketByNumber
    // =========================================================

    @Test
    @DisplayName("getTicketByNumber: existing ticket is returned")
    void getTicketByNumber_found_returnsTicket() {
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("ABC123456789");
        when(ticketDAO.findByTicketNumber("ABC123456789")).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicketByNumber("ABC123456789");
        assertNotNull(result);
    }

    @Test
    @DisplayName("getTicketByNumber: not found throws TicketGenerationException")
    void getTicketByNumber_notFound_throwsException() {
        when(ticketDAO.findByTicketNumber("INVALID")).thenReturn(Optional.empty());
        assertThrows(TicketGenerationException.class, () ->
                ticketService.getTicketByNumber("INVALID"));
    }

    @Test
    @DisplayName("getTicketByNumber: blank number throws TicketGenerationException")
    void getTicketByNumber_blank_throwsException() {
        assertThrows(TicketGenerationException.class, () ->
                ticketService.getTicketByNumber(""));
        assertThrows(TicketGenerationException.class, () ->
                ticketService.getTicketByNumber(null));
    }

    // =========================================================
    // getTicketsByUser
    // =========================================================

    @Test
    @DisplayName("getTicketsByUser: delegates to DAO")
    void getTicketsByUser_delegatesToDAO() {
        when(ticketDAO.findByUserId(1)).thenReturn(List.of());
        List<Ticket> result = ticketService.getTicketsByUser(1);
        assertNotNull(result);
        verify(ticketDAO).findByUserId(1);
    }
}
