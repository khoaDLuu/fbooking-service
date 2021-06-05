package com.filmbooking.booking_service.errors_handling;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ConstraintViolationAdvice {
    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String bookingNotFoundHandler(ConstraintViolationException ex) {
      return ex.getMessage();
    }
}
