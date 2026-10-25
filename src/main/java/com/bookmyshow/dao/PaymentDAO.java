package com.bookmyshow.dao;

import com.bookmyshow.model.Payment;

import java.util.Optional;

/**
 * DAO interface for Payment persistence.
 * Implemented by Member 1 using JDBC/PostgreSQL.
 */
public interface PaymentDAO {

    Payment create(Payment payment);

    Optional<Payment> findById(int id);

    Optional<Payment> findByBookingId(int bookingId);

    Payment update(Payment payment);
}
