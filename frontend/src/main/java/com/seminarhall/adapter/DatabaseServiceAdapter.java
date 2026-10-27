package com.seminarhall.adapter;

import com.seminarhall.model.*;
import com.seminarhall.service.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Service Adapter Template for integrating Member 2's PostgreSQL backend/DAO implementation.
 *
 * TO INTEGRATE WITH MEMBER 2'S SERVICE LAYER:
 * 1. Implement this class or pass Member 2's concrete services directly to:
 *    ServiceFactory.registerCustomServices(userService, eventService, seatService, bookingService, paymentService, ticketService);
 * 2. This decouples the Swing UI completely from JDBC, DAOs, or database connection pools.
 * 3. Never write SQL inside UI panels.
 */
public class DatabaseServiceAdapter {

    public static void activateDatabaseBackend(UserService dbUserService,
                                               EventService dbEventService,
                                               SeatService dbSeatService,
                                               BookingService dbBookingService,
                                               PaymentService dbPaymentService,
                                               TicketService dbTicketService) {
        ServiceFactory.registerCustomServices(
                dbUserService,
                dbEventService,
                dbSeatService,
                dbBookingService,
                dbPaymentService,
                dbTicketService
        );
        SessionManager.getInstance().setDemoMode(false);
    }
}
