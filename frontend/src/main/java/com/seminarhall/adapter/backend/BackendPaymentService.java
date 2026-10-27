package com.seminarhall.adapter.backend;

import com.seminarhall.service.PaymentService;
import com.seminarhall.service.ServiceException;

import java.util.UUID;

/**
 * Payment step of the UI flow.
 *
 * The UI asks for payment before it creates the booking, but the backend charges an
 * existing PENDING booking (so the amount comes from the backend, never the UI).
 * This step therefore only authorises the method; BackendBookingService performs the
 * real charge right after reserving the seats, and releases them if it is declined.
 */
public class BackendPaymentService implements PaymentService {

    @Override
    public PaymentResult processPayment(String paymentMethod, double amount, String payerIdentifier)
            throws ServiceException {
        if (amount < 0) {
            throw new ServiceException("Payment amount cannot be negative.");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            return new PaymentResult(false, null, "Please select a valid payment method.");
        }
        String authorisation = "AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, authorisation, "Payment method " + paymentMethod + " authorised");
    }
}
