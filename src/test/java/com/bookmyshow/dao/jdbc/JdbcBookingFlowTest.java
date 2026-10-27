package com.bookmyshow.dao.jdbc;

import com.bookmyshow.exception.SeatNotAvailableException;
import com.bookmyshow.model.Booking;
import com.bookmyshow.model.Event;
import com.bookmyshow.model.Seat;
import com.bookmyshow.model.Ticket;
import com.bookmyshow.model.User;
import com.bookmyshow.model.Venue;
import com.bookmyshow.model.enums.BookingStatus;
import com.bookmyshow.model.enums.Role;
import com.bookmyshow.model.enums.SeatStatus;
import com.bookmyshow.service.AuthServiceImpl;
import com.bookmyshow.service.BookingServiceImpl;
import com.bookmyshow.service.EventServiceImpl;
import com.bookmyshow.service.MockPaymentService;
import com.bookmyshow.service.PaymentResult;
import com.bookmyshow.service.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the real services against the JDBC DAOs on an in-memory H2 database,
 * so the SQL, the transactions and the service rules are tested together.
 */
@DisplayName("JDBC DAOs + services on H2")
class JdbcBookingFlowTest {

    private JdbcUserDAO userDAO;
    private JdbcVenueDAO venueDAO;
    private JdbcEventDAO eventDAO;
    private JdbcSeatDAO seatDAO;
    private JdbcBookingDAO bookingDAO;

    private AuthServiceImpl authService;
    private EventServiceImpl eventService;
    private BookingServiceImpl bookingService;
    private MockPaymentService paymentService;
    private TicketServiceImpl ticketService;

    private User organizer;
    private User customer;
    private Venue venue;
    private LocalDateTime start;

    @BeforeEach
    void setUp() {
        Database db = Database.h2InMemory("flow-" + UUID.randomUUID());
        db.initSchema();

        userDAO = new JdbcUserDAO(db);
        venueDAO = new JdbcVenueDAO(db);
        eventDAO = new JdbcEventDAO(db);
        seatDAO = new JdbcSeatDAO(db);
        bookingDAO = new JdbcBookingDAO(db);
        JdbcPaymentDAO paymentDAO = new JdbcPaymentDAO(db);
        JdbcTicketDAO ticketDAO = new JdbcTicketDAO(db);

        authService = new AuthServiceImpl(userDAO);
        eventService = new EventServiceImpl(eventDAO, venueDAO);
        bookingService = new BookingServiceImpl(bookingDAO, seatDAO, eventDAO);
        paymentService = new MockPaymentService(paymentDAO, bookingDAO, bookingService);
        ticketService = new TicketServiceImpl(ticketDAO, bookingDAO, eventDAO, userDAO);

        organizer = authService.registerUser("Org One", "org@college.edu", "secret1", Role.ORGANIZER);
        customer = authService.registerUser("Stu Dent", "stu@college.edu", "secret2", Role.CUSTOMER);
        venue = venueDAO.create(new Venue(0, "Seminar Hall A", "CS Block", "Campus", 120));
        start = LocalDateTime.now().plusDays(7).withNano(0);
    }

    private Event createEvent(String price, int seats) {
        return eventService.createEvent(organizer, "AI Seminar", "Talk", "TECHNICAL_SEMINAR",
                venue.getId(), start, start.plusHours(2), new BigDecimal(price), seats);
    }

    private List<Integer> seatIds(Event event, String... labels) {
        List<String> wanted = List.of(labels);
        return seatDAO.findByEventId(event.getId()).stream()
                .filter(s -> wanted.contains(s.getSeatLabel()))
                .map(Seat::getId)
                .collect(Collectors.toList());
    }

    private SeatStatus statusOf(Event event, String label) {
        return seatDAO.findByEventId(event.getId()).stream()
                .filter(s -> s.getSeatLabel().equals(label))
                .findFirst().orElseThrow().getStatus();
    }

    @Test
    @DisplayName("login works against stored users")
    void login_storedUser_succeeds() {
        User loggedIn = authService.login("STU@college.edu", "secret2");
        assertEquals(customer.getId(), loggedIn.getId());
        assertEquals(Role.CUSTOMER, loggedIn.getRole());
    }

    @Test
    @DisplayName("createEvent stores all fields and generates the seat map")
    void createEvent_generatesSeatsAndRoundTrips() {
        Event created = createEvent("200.00", 25);

        Event loaded = eventDAO.findById(created.getId()).orElseThrow();
        assertEquals("TECHNICAL_SEMINAR", loaded.getCategory());
        assertEquals(start, loaded.getEventDateTime());
        assertEquals(start.plusHours(2), loaded.getEndDateTime());
        assertEquals(0, new BigDecimal("200.00").compareTo(loaded.getTicketPrice()));
        assertEquals("Seminar Hall A", loaded.getVenue().getName());
        assertEquals(25, loaded.getAvailableSeats());

        List<String> labels = seatDAO.findByEventId(created.getId()).stream()
                .map(Seat::getSeatLabel).collect(Collectors.toList());
        assertEquals(25, labels.size());
        assertEquals(List.of("A1", "A10", "B1", "C5"),
                List.of(labels.get(0), labels.get(9), labels.get(10), labels.get(24)));
    }

    @Test
    @DisplayName("book → pay → ticket: booking confirmed, seats taken, ticket stored")
    void fullFlow_bookPayAndTicket() {
        Event event = createEvent("200.00", 20);

        Booking booking = bookingService.createBooking(customer, event.getId(), seatIds(event, "A1", "A2"));
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(SeatStatus.RESERVED, statusOf(event, "A1"));
        assertEquals(18, eventDAO.findById(event.getId()).orElseThrow().getAvailableSeats());

        PaymentResult result = paymentService.processPayment(booking.getId(), new BigDecimal("400.00"));
        assertTrue(result.isSuccess());
        assertEquals(BookingStatus.CONFIRMED, bookingDAO.findById(booking.getId()).orElseThrow().getStatus());

        Ticket ticket = ticketService.generateTicket(booking.getId());
        Ticket loaded = ticketService.getTicketByNumber(ticket.getTicketNumber());
        assertEquals(List.of("A1", "A2"), loaded.getSeatLabels());
        assertEquals("Stu Dent", loaded.getCustomerName());
        assertEquals("Seminar Hall A", loaded.getVenueName());
    }

    @Test
    @DisplayName("declined payment releases the reserved seats")
    void declinedPayment_releasesSeats() {
        Event event = createEvent("199.99", 20);
        Booking booking = bookingService.createBooking(customer, event.getId(), seatIds(event, "B3"));

        PaymentResult result = paymentService.processPayment(booking.getId(), new BigDecimal("199.99"));

        assertFalse(result.isSuccess());
        assertEquals(BookingStatus.FAILED, bookingDAO.findById(booking.getId()).orElseThrow().getStatus());
        assertEquals(SeatStatus.AVAILABLE, statusOf(event, "B3"));
        // and someone else can now book it
        assertNotNull(bookingService.createBooking(organizer, event.getId(), seatIds(event, "B3")));
    }

    @Test
    @DisplayName("cancelling a confirmed booking frees its seats for re-booking")
    void cancelConfirmed_seatsCanBeRebooked() {
        Event event = createEvent("100.00", 20);
        Booking booking = bookingService.createBooking(customer, event.getId(), seatIds(event, "B9"));
        paymentService.processPayment(booking.getId(), new BigDecimal("100.00"));

        bookingService.cancelBooking(booking.getId());

        assertEquals(BookingStatus.CANCELLED, bookingDAO.findById(booking.getId()).orElseThrow().getStatus());
        assertEquals(SeatStatus.AVAILABLE, statusOf(event, "B9"));
        Booking again = bookingService.createBooking(organizer, event.getId(), seatIds(event, "B9"));
        assertEquals(List.of("B9"), again.getBookingSeats().stream().map(s -> s.getSeatLabel()).collect(Collectors.toList()));
    }

    @Test
    @DisplayName("the same seat cannot be booked twice")
    void doubleBooking_isRejected() {
        Event event = createEvent("100.00", 20);
        bookingService.createBooking(customer, event.getId(), seatIds(event, "A5"));

        assertThrows(SeatNotAvailableException.class, () ->
                bookingService.createBooking(organizer, event.getId(), seatIds(event, "A5")));
    }

    @Test
    @DisplayName("reserveSeats is all-or-nothing")
    void reserveSeats_partialConflict_reservesNothing() {
        Event event = createEvent("100.00", 20);
        List<Integer> a1 = seatIds(event, "A1");
        List<Integer> a2 = seatIds(event, "A2");
        seatDAO.reserveSeats(a2);

        assertThrows(SeatNotAvailableException.class, () ->
                seatDAO.reserveSeats(List.of(a1.get(0), a2.get(0))));
        assertEquals(SeatStatus.AVAILABLE, statusOf(event, "A1"));
    }

    @Test
    @DisplayName("bookings are listed per user with their seats")
    void bookingsByUser_includeSeats() {
        Event event = createEvent("100.00", 20);
        bookingService.createBooking(customer, event.getId(), seatIds(event, "B5", "B6"));
        bookingService.createBooking(organizer, event.getId(), seatIds(event, "B7"));

        List<Booking> mine = bookingService.getBookingsByUser(customer.getId());
        assertEquals(1, mine.size());
        assertEquals(2, mine.get(0).getSeatCount());
    }

    @Test
    @DisplayName("data in an H2 file survives reopening the database")
    void fileDatabase_persistsAcrossRestarts(@TempDir Path dir) {
        Path file = dir.resolve("persist");
        Database first = Database.h2File(file);
        first.initSchema();
        new JdbcUserDAO(first).create(new User(0, "Keep Me", "keep@college.edu", "hash", Role.CUSTOMER));

        Database reopened = Database.h2File(file);
        reopened.initSchema();
        assertTrue(new JdbcUserDAO(reopened).existsByEmail("keep@college.edu"));
    }

    @Test
    @DisplayName("row labels continue past Z")
    void rowLabel_sequence() {
        assertEquals("A", JdbcEventDAO.rowLabel(0));
        assertEquals("Z", JdbcEventDAO.rowLabel(25));
        assertEquals("AA", JdbcEventDAO.rowLabel(26));
        assertEquals("AB", JdbcEventDAO.rowLabel(27));
    }
}
