package com.seminarhall.service;

import com.seminarhall.service.mock.*;

public class ServiceFactory {
    private static UserService userService;
    private static EventService eventService;
    private static SeatService seatService;
    private static BookingService bookingService;
    private static PaymentService paymentService;
    private static TicketService ticketService;

    static {
        initMockServices();
    }

    public static synchronized void initMockServices() {
        MockDataStore dataStore = MockDataStore.getInstance();
        userService = new MockUserService(dataStore);
        eventService = new MockEventService(dataStore);
        seatService = new MockSeatService(dataStore);
        bookingService = new MockBookingService(dataStore);
        paymentService = new MockPaymentService();
        ticketService = new MockTicketService(dataStore);
        SessionManager.getInstance().setDemoMode(true);
    }

    public static synchronized UserService getUserService() {
        return userService;
    }

    public static synchronized EventService getEventService() {
        return eventService;
    }

    public static synchronized SeatService getSeatService() {
        return seatService;
    }

    public static synchronized BookingService getBookingService() {
        return bookingService;
    }

    public static synchronized PaymentService getPaymentService() {
        return paymentService;
    }

    public static synchronized TicketService getTicketService() {
        return ticketService;
    }

    public static synchronized void registerCustomServices(UserService u, EventService e, SeatService s,
                                                            BookingService b, PaymentService p, TicketService t) {
        if (u != null) userService = u;
        if (e != null) eventService = e;
        if (s != null) seatService = s;
        if (b != null) bookingService = b;
        if (p != null) paymentService = p;
        if (t != null) ticketService = t;
        SessionManager.getInstance().setDemoMode(false);
    }
}
