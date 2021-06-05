package com.filmbooking.booking_service.errors_handling;

public class TicketNotFoundException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public TicketNotFoundException(Long id) {
      super("Could not find ticket " + id);
    }
}
