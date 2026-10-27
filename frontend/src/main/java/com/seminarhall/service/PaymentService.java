package com.seminarhall.service;

import java.util.UUID;

public interface PaymentService {
    class PaymentResult {
        private final boolean success;
        private final String transactionId;
        private final String message;

        public PaymentResult(boolean success, String transactionId, String message) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getMessage() {
            return message;
        }
    }

    PaymentResult processPayment(String paymentMethod, double amount, String payerIdentifier) throws ServiceException;
}
