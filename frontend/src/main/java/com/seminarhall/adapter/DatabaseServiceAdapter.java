package com.seminarhall.adapter;

import com.bookmyshow.dao.jdbc.Database;
import com.seminarhall.adapter.backend.BackendBookingService;
import com.seminarhall.adapter.backend.BackendContext;
import com.seminarhall.adapter.backend.BackendEventService;
import com.seminarhall.adapter.backend.BackendPaymentService;
import com.seminarhall.adapter.backend.BackendSeatService;
import com.seminarhall.adapter.backend.BackendTicketService;
import com.seminarhall.adapter.backend.BackendUserService;
import com.seminarhall.adapter.backend.DemoDataSeeder;
import com.seminarhall.service.*;

/**
 * Switches the UI from the in-memory demo services to Member 2's backend services
 * running on the JDBC data layer (com.bookmyshow.dao.jdbc).
 *
 * The Swing UI stays decoupled from JDBC: panels only ever see the interfaces in
 * com.seminarhall.service, and never contain SQL.
 */
public class DatabaseServiceAdapter {

    /**
     * Opens the database (creating tables and demo data on first run) and registers the
     * backend-backed services. Must be called before any screen is constructed, because
     * screens fetch their services from ServiceFactory when they are built.
     *
     * @throws RuntimeException if the database cannot be opened
     */
    public static void activateBackend(Database database) {
        BackendContext ctx = BackendContext.start(database);
        new DemoDataSeeder(ctx).seedIfEmpty();
        activateDatabaseBackend(
                new BackendUserService(ctx),
                new BackendEventService(ctx),
                new BackendSeatService(ctx),
                new BackendBookingService(ctx),
                new BackendPaymentService(),
                new BackendTicketService(ctx)
        );
    }

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
