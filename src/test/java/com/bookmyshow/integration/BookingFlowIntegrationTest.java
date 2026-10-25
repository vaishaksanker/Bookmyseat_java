package com.bookmyshow.integration;

import com.bookmyshow.dao.*;
import com.bookmyshow.exception.*;
import com.bookmyshow.model.*;
import com.bookmyshow.model.enums.*;
import com.bookmyshow.service.*;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * End-to-end integration test covering the complete booking flow:
 *
 * Register → Login → Get Event → Select Seats → Create Booking
 *  → Calculate Total → Process Payment → Generate Ticket → Retrieve Ticket
 *
 * All DAOs are mocked. This tests the entire service layer pipeline
 * without a real database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("End-to-End Booking Flow Integration Test")
class BookingFlowIntegrationTest {

    // --- DAO mocks ---
    @Mock private UserDAO userDAO;
    @Mock private EventDAO eventDAO;
    @Mock private VenueDAO venueDAO;
    @Mock private SeatDAO seatDAO;
    @Mock private BookingDAO bookingDAO;
    @Mock private PaymentDAO paymentDAO;
    @Mock private TicketDAO ticketDAO;

    // --- Services under test ---
    private AuthService authService;
    private EventService eventService;
    private BookingService bookingService;
    private PaymentService paymentService;
    private TicketService ticketService;

    // --- Test data ---
    private User registeredUser;
    private Venue venue;
    private Event event;
    private Seat seatA1;
    private Seat seatA2;

    @BeforeEach
    void setUp() {
        // Wire up the service layer (as Member 3 UI would do via DI)
        authService   = new AuthServiceImpl(userDAO);
        eventService  = new EventServiceImpl(eventDAO, venueDAO);
        bookingService = new BookingServiceImpl(bookingDAO, seatDAO, eventDAO);
        paymentService = new MockPaymentService(paymentDAO, bookingDAO, bookingService);
        ticketService  = new TicketServiceImpl(ticketDAO, bookingDAO, eventDAO, userDAO);

        // Shared test data
        venue = new Venue(10, "Grand Hall", "123 Main St", "Mumbai", 500);
        event = new Event(5, "Rock Concert", "Epic night of rock",
                venue, 99, LocalDateTime.now().plusDays(30),
                new BigDecimal("200.00"), 100);
        event.setStatus(EventStatus.PUBLISHED);
        event.setAvailableSeats(100);

        seatA1 = new Seat(101, 10, 5, "A", 1);
        seatA1.setStatus(SeatStatus.AVAILABLE);
        seatA2 = new Seat(102, 10, 5, "A", 2);
        seatA2.setStatus(SeatStatus.AVAILABLE);
    }

    // =========================================================
    // HAPPY PATH: Full booking flow
    // =========================================================

    @Test
    @DisplayName("Full Flow: Register → Login → Browse → Book → Pay → Get Ticket")
    void fullBookingFlow_happyPath_succeeds() {

        // ---- STEP 1: Register ----
        AuthServiceImpl authImpl = (AuthServiceImpl) authService;
        String passwordHash = authImpl.hashPassword("password123");

        when(userDAO.existsByEmail("alice@example.com")).thenReturn(false);
        registeredUser = new User(1, "Alice Smith", "alice@example.com",
                passwordHash, Role.CUSTOMER);
        when(userDAO.create(any(User.class))).thenReturn(registeredUser);

        User newUser = authService.registerUser("Alice Smith", "alice@example.com",
                "password123", Role.CUSTOMER);

        assertNotNull(newUser, "Registration should return a user");
        assertEquals("Alice Smith", newUser.getName());
        assertEquals(Role.CUSTOMER, newUser.getRole());

        // ---- STEP 2: Login ----
        when(userDAO.findByEmail("alice@example.com")).thenReturn(Optional.of(registeredUser));

        User loggedIn = authService.login("alice@example.com", "password123");

        assertNotNull(loggedIn, "Login should succeed");
        assertEquals("alice@example.com", loggedIn.getEmail());

        // ---- STEP 3: Browse events ----
        when(eventDAO.findByStatus(EventStatus.PUBLISHED)).thenReturn(List.of(event));

        List<Event> publishedEvents = eventService.getPublishedEvents();

        assertFalse(publishedEvents.isEmpty(), "Should find at least one published event");
        assertEquals("Rock Concert", publishedEvents.get(0).getName());

        // ---- STEP 4: Get event details ----
        when(eventDAO.findById(5)).thenReturn(Optional.of(event));

        Event selectedEvent = eventService.getEventById(5);

        assertEquals(5, selectedEvent.getId());
        assertTrue(selectedEvent.isBookable(), "Event should be bookable");

        // ---- STEP 5: Create booking ----
        when(seatDAO.findByIds(List.of(101, 102))).thenReturn(List.of(seatA1, seatA2));
        doNothing().when(seatDAO).reserveSeats(anyList());
        doNothing().when(bookingDAO).createBookingSeats(anyList());

        Booking pendingBooking = new Booking(1, loggedIn.getId(), 5, new BigDecimal("400.00"));
        // PENDING by default
        when(bookingDAO.create(any())).thenReturn(pendingBooking);

        Booking booking = bookingService.createBooking(loggedIn, 5, List.of(101, 102));

        assertNotNull(booking, "Booking should be created");
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(new BigDecimal("400.00"), booking.getTotalAmount(),
                "Total = 200.00 * 2 seats = 400.00");

        // ---- STEP 6: Process payment ----
        // The bookingService here is the real BookingServiceImpl, so bookingDAO.findById
        // must be stubbed before processPayment (which internally calls confirmBooking).
        when(bookingDAO.findById(1)).thenReturn(Optional.of(pendingBooking));
        when(paymentDAO.findByBookingId(1)).thenReturn(Optional.empty());

        Payment successPayment = new Payment(1, 1, new BigDecimal("400.00"),
                PaymentStatus.SUCCESS, "TXN-INTEGRATION-001");
        when(paymentDAO.create(any())).thenReturn(successPayment);

        // confirmBooking will be called inside processPayment → it updates booking to CONFIRMED
        Booking confirmedBooking = new Booking(1, loggedIn.getId(), 5, new BigDecimal("400.00"));
        confirmedBooking.setStatus(BookingStatus.CONFIRMED);
        confirmedBooking.addBookingSeat(new BookingSeat(1, 101, "A1"));
        confirmedBooking.addBookingSeat(new BookingSeat(1, 102, "A2"));
        when(bookingDAO.update(any())).thenReturn(confirmedBooking);

        PaymentResult paymentResult = paymentService.processPayment(1, new BigDecimal("400.00"));

        assertTrue(paymentResult.isSuccess(), "Payment should succeed");
        assertEquals(PaymentStatus.SUCCESS, paymentResult.getStatus());

        // ---- STEP 7: Generate ticket ----
        when(bookingDAO.findById(1)).thenReturn(Optional.of(confirmedBooking));
        when(ticketDAO.findByBookingId(1)).thenReturn(Optional.empty());
        when(eventDAO.findById(5)).thenReturn(Optional.of(event));
        when(userDAO.findById(1)).thenReturn(Optional.of(registeredUser));

        Ticket generatedTicket = new Ticket(1, loggedIn.getId(), 5,
                "Alice Smith", "Rock Concert", "Grand Hall",
                event.getEventDateTime(), List.of("A1", "A2"),
                new BigDecimal("400.00"));
        when(ticketDAO.create(any())).thenReturn(generatedTicket);

        Ticket ticket = ticketService.generateTicket(1);

        assertNotNull(ticket, "Ticket should be generated");
        assertEquals("Alice Smith", ticket.getCustomerName());
        assertEquals("Rock Concert", ticket.getEventName());
        assertEquals("Grand Hall", ticket.getVenueName());
        assertEquals(List.of("A1", "A2"), ticket.getSeatLabels());
        assertEquals(new BigDecimal("400.00"), ticket.getTotalAmount());
        assertEquals(PaymentStatus.SUCCESS, ticket.getPaymentStatus());

        // ---- STEP 8: Retrieve ticket ----
        when(ticketDAO.findByBookingId(1)).thenReturn(Optional.of(ticket));

        Ticket retrievedTicket = ticketService.getTicketByBookingId(1);

        assertNotNull(retrievedTicket);
        assertEquals(ticket.getEventName(), retrievedTicket.getEventName());

        // Verify the full pipeline was exercised
        verify(userDAO).create(any());          // registration
        verify(userDAO).findByEmail(any());     // login
        verify(eventDAO).findByStatus(any());   // browse
        verify(seatDAO).reserveSeats(any());    // seat reservation
        verify(bookingDAO).create(any());       // booking creation
        verify(paymentDAO).create(any());       // payment recording
        verify(ticketDAO).create(any());        // ticket generation
    }

    // =========================================================
    // FAILURE PATH: Payment fails → no ticket generated
    // =========================================================

    @Test
    @DisplayName("Payment failure: ticket must NOT be generated for a failed payment")
    void fullFlow_paymentFails_ticketNotGenerated() {

        // Set up a booking with a .99 amount to trigger simulated decline
        Booking pendingBooking = new Booking(2, 1, 5, new BigDecimal("399.99"));
        // bookingService is the real BookingServiceImpl — stub the DAO it uses
        when(bookingDAO.findById(2)).thenReturn(Optional.of(pendingBooking));
        when(paymentDAO.findByBookingId(2)).thenReturn(Optional.empty());

        Payment failedPayment = new Payment(1, 2, new BigDecimal("399.99"),
                PaymentStatus.FAILED, "TXN-DECLINED");
        when(paymentDAO.create(any())).thenReturn(failedPayment);
        when(bookingDAO.update(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResult result = paymentService.processPayment(2, new BigDecimal("399.99"));

        assertFalse(result.isSuccess(), "Payment should fail for .99 amount");

        // Now verify that trying to generate a ticket throws an exception
        Booking failedBooking = new Booking(2, 1, 5, new BigDecimal("399.99"));
        failedBooking.setStatus(BookingStatus.FAILED);
        when(bookingDAO.findById(2)).thenReturn(Optional.of(failedBooking));

        assertThrows(TicketGenerationException.class, () ->
                ticketService.generateTicket(2),
                "Should not generate ticket for a failed booking");

        verify(ticketDAO, never()).create(any());
    }

    // =========================================================
    // FAILURE PATH: Double-booking attempt
    // =========================================================

    @Test
    @DisplayName("Double-booking: second attempt on same seat is rejected")
    void fullFlow_doubleBooking_secondUserFails() {

        User userA = new User(1, "Alice", "alice@example.com", "hash", Role.CUSTOMER);
        User userB = new User(2, "Bob", "bob@example.com", "hash", Role.CUSTOMER);

        // User A books successfully
        when(eventDAO.findById(5)).thenReturn(Optional.of(event));
        when(seatDAO.findByIds(List.of(101))).thenReturn(List.of(seatA1));
        doNothing().when(seatDAO).reserveSeats(anyList());
        doNothing().when(bookingDAO).createBookingSeats(anyList());

        Booking bookingA = new Booking(1, userA.getId(), 5, new BigDecimal("200.00"));
        when(bookingDAO.create(any())).thenReturn(bookingA);

        Booking resultA = bookingService.createBooking(userA, 5, List.of(101));
        assertNotNull(resultA, "User A's booking should succeed");

        // Now seat A1 is BOOKED (simulate what DAO would do)
        seatA1.setStatus(SeatStatus.BOOKED);

        // User B tries to book the same seat — service-layer check catches it
        when(seatDAO.findByIds(List.of(101))).thenReturn(List.of(seatA1));

        assertThrows(SeatNotAvailableException.class, () ->
                bookingService.createBooking(userB, 5, List.of(101)),
                "User B should be rejected — seat already booked");
    }
}
