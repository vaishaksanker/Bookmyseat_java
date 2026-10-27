package com.seminarhall.adapter.backend;

import com.bookmyshow.dao.jdbc.Database;
import com.bookmyshow.dao.jdbc.JdbcBookingDAO;
import com.bookmyshow.dao.jdbc.JdbcEventDAO;
import com.bookmyshow.dao.jdbc.JdbcPaymentDAO;
import com.bookmyshow.dao.jdbc.JdbcSeatDAO;
import com.bookmyshow.dao.jdbc.JdbcTicketDAO;
import com.bookmyshow.dao.jdbc.JdbcUserDAO;
import com.bookmyshow.dao.jdbc.JdbcVenueDAO;
import com.bookmyshow.service.AuthService;
import com.bookmyshow.service.AuthServiceImpl;
import com.bookmyshow.service.BookingService;
import com.bookmyshow.service.BookingServiceImpl;
import com.bookmyshow.service.EventService;
import com.bookmyshow.service.EventServiceImpl;
import com.bookmyshow.service.MockPaymentService;
import com.bookmyshow.service.PaymentService;
import com.bookmyshow.service.TicketService;
import com.bookmyshow.service.TicketServiceImpl;

/**
 * Wires Member 1's JDBC DAOs into Member 2's services, in one place.
 * The UI adapters in this package only ever talk to what this holds.
 */
public class BackendContext {

    final JdbcUserDAO userDAO;
    final JdbcVenueDAO venueDAO;
    final JdbcEventDAO eventDAO;
    final JdbcSeatDAO seatDAO;
    final JdbcBookingDAO bookingDAO;
    final JdbcPaymentDAO paymentDAO;
    final JdbcTicketDAO ticketDAO;

    final AuthService authService;
    final EventService eventService;
    final BookingService bookingService;
    final PaymentService paymentService;
    final TicketService ticketService;

    private BackendContext(Database db) {
        userDAO = new JdbcUserDAO(db);
        venueDAO = new JdbcVenueDAO(db);
        eventDAO = new JdbcEventDAO(db);
        seatDAO = new JdbcSeatDAO(db);
        bookingDAO = new JdbcBookingDAO(db);
        paymentDAO = new JdbcPaymentDAO(db);
        ticketDAO = new JdbcTicketDAO(db);

        authService = new AuthServiceImpl(userDAO);
        eventService = new EventServiceImpl(eventDAO, venueDAO);
        bookingService = new BookingServiceImpl(bookingDAO, seatDAO, eventDAO);
        paymentService = new MockPaymentService(paymentDAO, bookingDAO, bookingService);
        ticketService = new TicketServiceImpl(ticketDAO, bookingDAO, eventDAO, userDAO);
    }

    /** Creates any missing tables, then builds the DAOs and services. */
    public static BackendContext start(Database db) {
        db.initSchema();
        return new BackendContext(db);
    }
}
