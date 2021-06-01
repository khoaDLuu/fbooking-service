package com.filmbooking.booking_service;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.ApiOperation;

import com.filmbooking.booking_service.models.Ticket;
import com.filmbooking.booking_service.utils.authHeader.DefaultAuthHeader;
import com.filmbooking.booking_service.utils.permission.operation.SimpleOperation;

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
        @RequestHeader HttpHeaders ppHeaders,
        @RequestParam(value = "screening_id", required = false)
        String screening_id
    ) {
        if (new DefaultAuthHeader(ppHeaders).token().claims().perms().forbid(
            new SimpleOperation("TICKET.READ")
        )) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on TICKET, " +
                "or your token is not valid"
            );
        }

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
    Ticket one(@RequestHeader HttpHeaders ppHeaders, @PathVariable Long id) {
        if (new DefaultAuthHeader(ppHeaders).token().claims().perms().forbid(
            new SimpleOperation("TICKET.READ")
        )) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on TICKET, " +
                "or your token is not valid"
            );
        }

        return repository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }
}
