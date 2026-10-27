package com.seminarhall.service.mock;

import com.seminarhall.model.Booking;
import com.seminarhall.model.Ticket;
import com.seminarhall.service.ServiceException;
import com.seminarhall.service.TicketService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MockTicketService implements TicketService {
    private final MockDataStore dataStore;

    public MockTicketService(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public List<Ticket> generateTicketsForBooking(Booking booking) throws ServiceException {
        if (booking == null) throw new ServiceException("Booking cannot be null.");
        List<Ticket> result = new ArrayList<>();
        for (Ticket t : dataStore.getTickets().values()) {
            if (t.getBookingId().equals(booking.getId())) {
                result.add(t);
            }
        }
        return result;
    }

    @Override
    public Ticket getTicketByNumber(String ticketNumber) throws ServiceException {
        if (ticketNumber == null) throw new ServiceException("Ticket number is required.");
        Ticket t = dataStore.getTickets().get(ticketNumber);
        if (t == null) throw new ServiceException("Ticket not found: " + ticketNumber);
        return t;
    }

    @Override
    public byte[] exportTicketPdf(String ticketNumber) throws ServiceException {
        Ticket t = getTicketByNumber(ticketNumber);
        String mockPdfContent = "--- DIGITAL SEMINAR PASS ---\n" +
                "TICKET NUMBER: " + t.getTicketNumber() + "\n" +
                "EVENT: " + t.getEventTitle() + "\n" +
                "VENUE: " + t.getVenue() + " (" + t.getSeminarHall() + ")\n" +
                "DATE & TIME: " + t.getEventDateTime() + "\n" +
                "ATTENDEE: " + t.getAttendeeName() + "\n" +
                "SEAT: " + t.getSeatIdentifier() + "\n" +
                "PRICE: Rs. " + t.getPrice() + "\n" +
                "VERIFICATION QR PAYLOAD: " + t.getQrPayload() + "\n" +
                "----------------------------\n";
        return mockPdfContent.getBytes(StandardCharsets.UTF_8);
    }
}
