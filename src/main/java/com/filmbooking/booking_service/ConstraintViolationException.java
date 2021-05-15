package com.filmbooking.booking_service;

public class ConstraintViolationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    ConstraintViolationException(String details) {
      super("Violation of DB constraint: " + details);
    }
}
