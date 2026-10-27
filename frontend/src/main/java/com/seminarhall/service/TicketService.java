package com.seminarhall.service;

import com.seminarhall.model.Booking;
import com.seminarhall.model.Ticket;

import java.util.List;

public interface TicketService {
    List<Ticket> generateTicketsForBooking(Booking booking) throws ServiceException;
    Ticket getTicketByNumber(String ticketNumber) throws ServiceException;
    byte[] exportTicketPdf(String ticketNumber) throws ServiceException;
}
