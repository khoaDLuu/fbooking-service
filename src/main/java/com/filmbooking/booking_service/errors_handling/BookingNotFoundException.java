package com.filmbooking.booking_service.errors_handling;

public class BookingNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BookingNotFoundException(Long id) {
      super("Could not find booking " + id);
    }
}
