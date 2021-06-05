package com.filmbooking.booking_service.errors_handling;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class TicketNotFoundAdvice {

  @ResponseBody
  @ExceptionHandler(TicketNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String ticketNotFoundHandler(TicketNotFoundException ex) {
    return ex.getMessage();
  }
}
