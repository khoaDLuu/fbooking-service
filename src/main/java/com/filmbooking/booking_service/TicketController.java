package com.filmbooking.booking_service;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    List<Ticket> all(
        @RequestParam(value = "screening_id", required = false)
        String screening_id
    ) {
        if (screening_id != null) {
            return repository.findByScreening(Long.parseLong(screening_id));
        }
        else {
            return repository.findAll();
        }
    }

    @PostMapping("/tickets")
    Ticket newTicket(@RequestBody Ticket newTicket) {
        return repository.save(newTicket);
    }

    @GetMapping("/tickets/{id}")
    Ticket one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }

    @PutMapping("/tickets/{id}")
    Ticket updateTicket(@RequestBody Ticket newTicket, @PathVariable Long id) {
        return repository.findById(id).map(ticket -> {
            ticket.setBooking(newTicket.getBooking());
            ticket.setScreeningId(newTicket.getScreeningId());
            ticket.setSeatNumber(newTicket.getSeatNumber());
            return repository.save(ticket);
        }).orElseGet(() -> {
            return repository.save(newTicket);
        });
    }

    @DeleteMapping("/tickets/{id}")
    void deleteTicket(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
