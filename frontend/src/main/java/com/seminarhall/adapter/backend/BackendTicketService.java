package com.seminarhall.adapter.backend;

import com.seminarhall.model.Booking;
import com.seminarhall.model.Ticket;
import com.seminarhall.service.ServiceException;
import com.seminarhall.service.TicketService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.seminarhall.adapter.backend.Mappers.call;
import static com.seminarhall.adapter.backend.Mappers.parseId;

/**
 * TicketService backed by the backend TicketService.
 *
 * The backend issues one ticket per booking listing all its seats; the UI shows one
 * pass per seat, numbered "<backend ticket number>-<seat>" (e.g. 3F2A9C01B7D4-A5).
 */
public class BackendTicketService implements TicketService {

    private final BackendContext ctx;

    public BackendTicketService(BackendContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public List<Ticket> generateTicketsForBooking(Booking booking) throws ServiceException {
        if (booking == null) {
            throw new ServiceException("Booking cannot be null.");
        }
        int bookingId = parseId(booking.getId(), "Booking");
        return call(() -> {
            com.bookmyshow.model.Ticket ticket = ctx.ticketDAO.findByBookingId(bookingId)
                    .orElseGet(() -> ctx.ticketService.generateTicket(bookingId));
            List<Ticket> passes = new ArrayList<>();
            for (String seat : ticket.getSeatLabels()) {
                passes.add(toPass(ticket, seat));
            }
            return passes;
        });
    }

    @Override
    public Ticket getTicketByNumber(String ticketNumber) throws ServiceException {
        if (ticketNumber == null) {
            throw new ServiceException("Ticket number is required.");
        }
        int dash = ticketNumber.lastIndexOf('-');
        if (dash <= 0) {
            throw new ServiceException("Ticket not found: " + ticketNumber);
        }
        String number = ticketNumber.substring(0, dash);
        String seat = ticketNumber.substring(dash + 1);
        return call(() -> {
            com.bookmyshow.model.Ticket ticket = ctx.ticketService.getTicketByNumber(number);
            if (!ticket.getSeatLabels().contains(seat)) {
                throw new ServiceException("Ticket not found: " + ticketNumber);
            }
            return toPass(ticket, seat);
        });
    }

    @Override
    public byte[] exportTicketPdf(String ticketNumber) throws ServiceException {
        Ticket t = getTicketByNumber(ticketNumber);
        String content = "--- DIGITAL SEMINAR PASS ---\n" +
                "TICKET NUMBER: " + t.getTicketNumber() + "\n" +
                "EVENT: " + t.getEventTitle() + "\n" +
                "VENUE: " + t.getVenue() + " (" + t.getSeminarHall() + ")\n" +
                "DATE & TIME: " + t.getEventDateTime() + "\n" +
                "ATTENDEE: " + t.getAttendeeName() + "\n" +
                "SEAT: " + t.getSeatIdentifier() + "\n" +
                "PRICE: Rs. " + t.getPrice() + "\n" +
                "VERIFICATION QR PAYLOAD: " + t.getQrPayload() + "\n" +
                "----------------------------\n";
        return content.getBytes(StandardCharsets.UTF_8);
    }

    private Ticket toPass(com.bookmyshow.model.Ticket ticket, String seat) {
        com.bookmyshow.model.Event event = ctx.eventDAO.findById(ticket.getEventId()).orElse(null);
        com.bookmyshow.model.User user = ctx.userDAO.findById(ticket.getUserId()).orElse(null);
        com.seminarhall.model.Event uiEvent = event != null ? Mappers.event(event, null) : null;
        String passNumber = ticket.getTicketNumber() + "-" + seat;
        return new Ticket(
                passNumber,
                Mappers.bookingId(ticket.getBookingId()),
                ticket.getEventName(),
                uiEvent != null ? uiEvent.getVenue() : ticket.getVenueName(),
                ticket.getVenueName(),
                ticket.getEventDateTime(),
                ticket.getCustomerName(),
                user != null ? user.getEmail() : "",
                seat,
                event != null ? event.getTicketPrice().doubleValue() : 0.0,
                "BOOKMYSEAT:EVENT:" + ticket.getEventId() + ":TKT:" + passNumber
        );
    }
}
