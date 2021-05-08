package com.filmbooking.booking_service;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

import com.filmbooking.booking_service.models.Ticket;

@RestController
class TicketController {

    private final TicketRepository repository;

    TicketController(TicketRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/tickets")
    @ApiOperation(
        value = "retrieve tickets",
        response = Ticket.class,
        responseContainer = "List"
    )
    ResponseWrapper<Ticket> all(
        @RequestParam(value = "screening_id", required = false)
        String screening_id
    ) {
        List<Ticket> unwrapped = null;
        if (screening_id != null) {
            unwrapped = repository.findByScreening(Long.parseLong(screening_id));
        }
        else {
            unwrapped = repository.findAll();
        }
        return new ResponseWrapper<Ticket>(unwrapped);
    }

    @GetMapping("/tickets/{id}")
    Ticket one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }
}