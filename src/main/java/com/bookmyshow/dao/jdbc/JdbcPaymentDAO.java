package com.bookmyshow.dao.jdbc;

import com.bookmyshow.dao.PaymentDAO;
import com.bookmyshow.model.Payment;
import com.bookmyshow.model.enums.PaymentStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * JDBC implementation of {@link PaymentDAO}. One payment row per booking (UNIQUE booking_id).
 */
public class JdbcPaymentDAO extends JdbcSupport implements PaymentDAO {

    private static final String COLUMNS =
            "id, booking_id, amount, status, transaction_reference, failure_reason, payment_time";

    public JdbcPaymentDAO(Database db) {
        super(db);
    }

    @Override
    public Payment create(Payment payment) {
        int id = insert("INSERT INTO payments (booking_id, amount, status, transaction_reference, "
                        + "failure_reason, payment_time) VALUES (?, ?, ?, ?, ?, ?)",
                ps -> {
                    ps.setInt(1, payment.getBookingId());
                    ps.setBigDecimal(2, payment.getAmount());
                    ps.setString(3, payment.getStatus().name());
                    ps.setString(4, payment.getTransactionReference());
                    ps.setString(5, payment.getFailureReason());
                    ps.setTimestamp(6, ts(payment.getPaymentTime()));
                });
        payment.setId(id);
        return payment;
    }

    @Override
    public Optional<Payment> findById(int id) {
        return queryOne("SELECT " + COLUMNS + " FROM payments WHERE id = ?",
                ps -> ps.setInt(1, id), JdbcPaymentDAO::map);
    }

    @Override
    public Optional<Payment> findByBookingId(int bookingId) {
        return queryOne("SELECT " + COLUMNS + " FROM payments WHERE booking_id = ?",
                ps -> ps.setInt(1, bookingId), JdbcPaymentDAO::map);
    }

    @Override
    public Payment update(Payment payment) {
        update("UPDATE payments SET status = ?, transaction_reference = ?, failure_reason = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, payment.getStatus().name());
                    ps.setString(2, payment.getTransactionReference());
                    ps.setString(3, payment.getFailureReason());
                    ps.setInt(4, payment.getId());
                });
        return payment;
    }

    private static Payment map(ResultSet rs) throws SQLException {
        Payment payment = new Payment(rs.getInt("id"), rs.getInt("booking_id"), rs.getBigDecimal("amount"),
                PaymentStatus.valueOf(rs.getString("status")), rs.getString("transaction_reference"));
        payment.setFailureReason(rs.getString("failure_reason"));
        payment.setPaymentTime(ldt(rs.getTimestamp("payment_time")));
        return payment;
    }
}
