package com.filmbooking.booking_service;

public class TicketNotFoundException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    TicketNotFoundException(Long id) {
      super("Could not find ticket " + id);
    }
}
