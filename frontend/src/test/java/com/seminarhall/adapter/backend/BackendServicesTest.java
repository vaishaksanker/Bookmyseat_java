package com.seminarhall.adapter.backend;

import com.bookmyshow.dao.jdbc.Database;
import com.seminarhall.model.Booking;
import com.seminarhall.model.BookingStatus;
import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import com.seminarhall.model.OrganizerAnalytics;
import com.seminarhall.model.Seat;
import com.seminarhall.model.SeatStatus;
import com.seminarhall.model.Ticket;
import com.seminarhall.model.User;
import com.seminarhall.model.UserRole;
import com.seminarhall.service.ServiceException;
import com.seminarhall.service.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The UI-facing services running on the real backend services and an in-memory H2 database,
 * seeded with the same demo data the app uses.
 */
@DisplayName("UI services on the real backend")
class BackendServicesTest {

    private BackendUserService users;
    private BackendEventService events;
    private BackendSeatService seats;
    private BackendBookingService bookings;
    private BackendPaymentService payments;
    private BackendTicketService tickets;

    private User student;

    @BeforeEach
    void setUp() throws ServiceException {
        BackendContext ctx = BackendContext.start(Database.h2InMemory("ui-" + UUID.randomUUID()));
        assertTrue(new DemoDataSeeder(ctx).seedIfEmpty());
        users = new BackendUserService(ctx);
        events = new BackendEventService(ctx);
        seats = new BackendSeatService(ctx);
        bookings = new BackendBookingService(ctx);
        payments = new BackendPaymentService();
        tickets = new BackendTicketService(ctx);

        student = users.authenticate(DemoDataSeeder.STUDENT_EMAIL, DemoDataSeeder.STUDENT_PASSWORD);
        SessionManager.getInstance().setCurrentUser(student);
    }

    @AfterEach
    void tearDown() {
        SessionManager.getInstance().logout();
    }

    private Event eventTitled(String title) throws ServiceException {
        return events.getAllEvents().stream()
                .filter(e -> e.getTitle().equals(title))
                .findFirst().orElseThrow();
    }

    private List<String> seatIds(Event event, String... labels) throws ServiceException {
        List<String> wanted = List.of(labels);
        return seats.getSeatsForEvent(event.getId()).stream()
                .filter(s -> wanted.contains(s.getSeatIdentifier()))
                .map(Seat::getId)
                .collect(Collectors.toList());
    }

    private SeatStatus statusOf(Event event, String label) throws ServiceException {
        return seats.getSeatsForEvent(event.getId()).stream()
                .filter(s -> s.getSeatIdentifier().equals(label))
                .findFirst().orElseThrow().getStatus();
    }

    /** Same two calls PaymentPanel makes. */
    private Booking payAndBook(Event event, User user, List<String> seatIds) throws ServiceException {
        var auth = payments.processPayment("UPI", 0, user.getEmail());
        assertTrue(auth.isSuccess());
        return bookings.createBooking(event.getId(), user.getId(), seatIds, "UPI", auth.getTransactionId());
    }

    @Test
    @DisplayName("demo accounts log in with the documented passwords and roles")
    void demoAccounts_logIn() throws ServiceException {
        assertEquals(UserRole.STUDENT, student.getRole());
        assertEquals("Ananthakrishna S", student.getFullName());
        User organizer = users.authenticate(DemoDataSeeder.ORGANIZER_EMAIL, DemoDataSeeder.ORGANIZER_PASSWORD);
        assertEquals(UserRole.ORGANIZER, organizer.getRole());
        assertThrows(ServiceException.class, () -> users.authenticate(DemoDataSeeder.STUDENT_EMAIL, "wrong-pass"));
    }

    @Test
    @DisplayName("registration stores a new student who can then log in")
    void register_thenLogin() throws ServiceException {
        users.register("New Student", "new@college.edu", "+91 90000 00000", "pass1234", UserRole.STUDENT);
        assertTrue(users.isEmailRegistered("NEW@college.edu"));
        assertEquals("New Student", users.authenticate("new@college.edu", "pass1234").getFullName());
        assertThrows(ServiceException.class, () ->
                users.register("Dup Student", "new@college.edu", "", "pass1234", UserRole.STUDENT));
    }

    @Test
    @DisplayName("all 12 demo events load with categories, halls and organizers")
    void events_loadWithDetails() throws ServiceException {
        List<Event> all = events.getAllEvents();
        assertEquals(12, all.size());
        assertEquals(3, events.getEventsByCategory(EventCategory.WORKSHOP).size());

        Event cultural = eventTitled("Annual Cultural Meet");
        assertEquals(EventCategory.CULTURAL, cultural.getCategory());
        assertEquals("Main Auditorium", cultural.getSeminarHall());
        assertEquals("Campus Cultural Center", cultural.getVenue());
        assertEquals("Fine Arts Club", cultural.getOrganizerName());
        assertEquals(LocalTime.of(18, 0), cultural.getStartTime());
        assertEquals(LocalTime.of(21, 30), cultural.getEndTime());
        assertEquals(1, cultural.getBookedSeats());   // seeded booking C3
    }

    @Test
    @DisplayName("search filters by text and category and sorts by price")
    void search_filtersAndSorts() throws ServiceException {
        assertEquals(List.of("Robotics Workshop"),
                events.searchEvents("ros 2", EventCategory.ALL, "Date").stream()
                        .map(Event::getTitle).collect(Collectors.toList()));
        List<Event> byPrice = events.searchEvents("", EventCategory.WORKSHOP, "Price (High to Low)");
        assertEquals("Robotics Workshop", byPrice.get(0).getTitle());
        assertEquals("Web Development Bootcamp", byPrice.get(2).getTitle());
    }

    @Test
    @DisplayName("seat map reflects existing bookings")
    void seatMap_showsBookedSeats() throws ServiceException {
        Event bootcamp = eventTitled("Web Development Bootcamp");
        List<Seat> map = seats.getSeatsForEvent(bootcamp.getId());
        assertEquals(100, map.size());
        assertEquals(SeatStatus.BOOKED, statusOf(bootcamp, "A1"));
        assertEquals(SeatStatus.AVAILABLE, statusOf(bootcamp, "A3"));
        assertEquals(100.0, map.get(0).getPrice());
    }

    @Test
    @DisplayName("paid booking is confirmed, charged by the backend and issues passes")
    void paidBooking_confirmedWithTickets() throws ServiceException {
        Event robotics = eventTitled("Robotics Workshop");

        Booking booking = payAndBook(robotics, student, seatIds(robotics, "E4", "E5"));

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(400.0, booking.getTotalAmount());
        assertEquals(List.of("E4", "E5"), booking.getSeatIdentifiers());
        assertEquals(SeatStatus.BOOKED, statusOf(robotics, "E4"));
        assertTrue(booking.getTransactionId().startsWith("TXN-"));

        List<Ticket> passes = tickets.generateTicketsForBooking(booking);
        assertEquals(2, passes.size());
        Ticket pass = tickets.getTicketByNumber(passes.get(0).getTicketNumber());
        assertEquals("E4", pass.getSeatIdentifier());
        assertEquals("Robotics Lab", pass.getSeminarHall());
        assertEquals(List.of("E4", "E5"), bookings.getBookingById(booking.getId()).getSeatIdentifiers());
    }

    @Test
    @DisplayName("free events are confirmed without a payment")
    void freeBooking_confirmedWithoutCharge() throws ServiceException {
        Event summit = eventTitled("Cybersecurity Summit");
        Booking booking = payAndBook(summit, student, seatIds(summit, "A3"));
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(0.0, booking.getTotalAmount());
        assertEquals("Free Registration", booking.getPaymentMethod());
    }

    @Test
    @DisplayName("declined payment shows an error and frees the seats")
    void declinedPayment_releasesSeats() throws ServiceException {
        SessionManager.getInstance().setCurrentUser(
                users.authenticate(DemoDataSeeder.ORGANIZER_EMAIL, DemoDataSeeder.ORGANIZER_PASSWORD));
        Event pricey = events.createEvent(newEvent("Gala Dinner", 99.99, 20));

        ServiceException error = assertThrows(ServiceException.class, () ->
                payAndBook(pricey, student, seatIds(pricey, "A1")));

        assertTrue(error.getMessage().contains("declined"), error.getMessage());
        assertEquals(SeatStatus.AVAILABLE, statusOf(pricey, "A1"));
        assertTrue(bookings.getBookingsForUser(student.getId()).stream()
                .noneMatch(b -> b.getEventId().equals(pricey.getId())));
    }

    @Test
    @DisplayName("a seat booked by someone else cannot be booked again")
    void doubleBooking_rejectedWithClearMessage() throws ServiceException {
        Event bootcamp = eventTitled("Web Development Bootcamp");
        ServiceException error = assertThrows(ServiceException.class, () ->
                payAndBook(bootcamp, student, seatIds(bootcamp, "A1")));
        assertTrue(error.getMessage().contains("just booked"), error.getMessage());
    }

    @Test
    @DisplayName("booking history only contains the user's own bookings")
    void history_isPerUser() throws ServiceException {
        List<Booking> mine = bookings.getBookingsForUser(student.getId());
        assertEquals(4, mine.size());
        assertEquals(1, mine.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count());

        User other = users.register("Other Person", "other@college.edu", "", "pass1234", UserRole.STUDENT);
        assertTrue(bookings.getBookingsForUser(other.getId()).isEmpty());
        assertEquals(4, bookings.getAllBookings().size());
    }

    @Test
    @DisplayName("cancelling a booking frees its seats")
    void cancel_releasesSeats() throws ServiceException {
        Event robotics = eventTitled("Robotics Workshop");
        Booking booking = payAndBook(robotics, student, seatIds(robotics, "F1"));

        bookings.cancelBooking(booking.getId());

        assertEquals(BookingStatus.CANCELLED, bookings.getBookingById(booking.getId()).getStatus());
        assertEquals(SeatStatus.AVAILABLE, statusOf(robotics, "F1"));
    }

    @Test
    @DisplayName("organizer-created events are stored with their category and seat map")
    void organizerCreatesEvent() throws ServiceException {
        SessionManager.getInstance().setCurrentUser(
                users.authenticate(DemoDataSeeder.ORGANIZER_EMAIL, DemoDataSeeder.ORGANIZER_PASSWORD));

        Event created = events.createEvent(newEvent("Quantum Computing 101", 0, 35));

        Event loaded = events.getEventById(created.getId());
        assertEquals(EventCategory.TECHNICAL_SEMINAR, loaded.getCategory());
        assertEquals("Faculty Coordinator", loaded.getOrganizerName());
        assertEquals(LocalTime.of(12, 0), loaded.getEndTime());
        assertEquals(35, seats.getSeatsForEvent(created.getId()).size());
        assertEquals(13, events.getAllEvents().size());
    }

    @Test
    @DisplayName("students cannot create events")
    void studentCannotCreateEvent() {
        assertThrows(ServiceException.class, () -> events.createEvent(newEvent("Not Allowed", 0, 10)));
    }

    @Test
    @DisplayName("organizer analytics are computed from stored bookings")
    void analytics_fromStoredData() throws ServiceException {
        OrganizerAnalytics analytics = bookings.getOrganizerAnalytics();
        assertEquals(12, analytics.getTotalEvents());
        assertEquals(4, analytics.getTotalBookings());
        assertEquals(350.0, analytics.getTotalRevenue());   // bootcamp 2 x 100 + cultural meet 150
        assertEquals(6, analytics.getBookedSeats());          // B5-B7, A1-A2, C3 (D1-D2 cancelled)
    }

    private static Event newEvent(String title, double price, int capacity) {
        Event event = new Event();
        event.setTitle(title);
        event.setCategory(EventCategory.TECHNICAL_SEMINAR);
        event.setDescription("Created in a test");
        event.setDate(LocalDate.now().plusDays(20));
        event.setStartTime(LocalTime.of(10, 0));
        event.setEndTime(LocalTime.of(12, 0));
        event.setVenue("Physics Block");
        event.setSeminarHall("Seminar Hall C");
        event.setTotalCapacity(capacity);
        event.setTicketPrice(price);
        return event;
    }
}
