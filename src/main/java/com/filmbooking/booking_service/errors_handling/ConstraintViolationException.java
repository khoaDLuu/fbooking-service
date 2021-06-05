package com.filmbooking.booking_service.errors_handling;

public class ConstraintViolationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConstraintViolationException(String details) {
      super("Violation of DB constraint: " + details);
    }
}
