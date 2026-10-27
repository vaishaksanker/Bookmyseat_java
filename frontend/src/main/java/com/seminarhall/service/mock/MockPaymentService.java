package com.seminarhall.service.mock;

import com.seminarhall.service.PaymentService;
import com.seminarhall.service.ServiceException;

import java.util.UUID;

public class MockPaymentService implements PaymentService {

    @Override
    public PaymentResult processPayment(String paymentMethod, double amount, String payerIdentifier) throws ServiceException {
        if (amount < 0) {
            throw new ServiceException("Payment amount cannot be negative.");
        }

        // Simulate slight network processing delay safely
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            return new PaymentResult(false, null, "Please select a valid payment method.");
        }

        // Mock failure test cases if user enters "fail"
        if (payerIdentifier != null && payerIdentifier.toLowerCase().contains("fail")) {
            return new PaymentResult(false, null, "Payment declined by simulated payment provider (Simulated Failure).");
        }

        String txnId = "SIM-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, txnId, "Payment of ₹" + String.format("%.2f", amount) + " simulated successfully via " + paymentMethod);
    }
}
