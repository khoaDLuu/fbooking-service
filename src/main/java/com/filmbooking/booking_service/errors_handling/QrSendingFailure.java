package com.filmbooking.booking_service.errors_handling;

public class QrSendingFailure extends Exception {
    public QrSendingFailure(String message) {
      super(message);
    }
}
