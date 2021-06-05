package com.filmbooking.booking_service.errors_handling;

public class PaypalTransactionException extends Exception {
    public PaypalTransactionException(String message) {
      super(message);
    }
}
