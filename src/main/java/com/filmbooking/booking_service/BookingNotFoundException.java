package com.filmbooking.booking_service;

public class BookingNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    BookingNotFoundException(Long id) {
      super("Could not find booking " + id);
    }
}
