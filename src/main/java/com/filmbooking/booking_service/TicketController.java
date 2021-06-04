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
import com.filmbooking.booking_service.utils.token.claims.Claims;
import com.filmbooking.booking_service.utils.user.role.SimpleRole;

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
        String screeningId
    ) {
        Claims allClaims = new DefaultAuthHeader(ppHeaders).token().claims();

        if (allClaims.perms().forbid(new SimpleOperation("TICKET.READ"))) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on TICKET, " +
                "or your token is not valid"
            );
        }

        if (allClaims.requester().roles().sameAs(new SimpleRole("ROLE_GUEST"))) {
            throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Guest users are not allowed to view all tickets"
            );
        }

        try {
            List<Ticket> unwrapped = null;
            if (screeningId != null) {
                unwrapped = repository.findByScreening(Long.parseLong(screeningId));
            }
            else {
                unwrapped = repository.findAll();
            }
            return new ResponseWrapper<Ticket>(unwrapped);
        }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Malformed query param value: 'screening_id'"
            );
        }
    }

    @GetMapping("/tickets/{id}")
    ResponseWrapperSingle<Ticket> one(@RequestHeader HttpHeaders ppHeaders, @PathVariable Long id) {
        Claims allClaims = new DefaultAuthHeader(ppHeaders).token().claims();

        if (allClaims.perms().forbid(new SimpleOperation("TICKET.READ"))) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on TICKET, " +
                "or your token is not valid"
            );
        }

        if (allClaims.requester().roles().sameAs(new SimpleRole("ROLE_GUEST"))) {
            throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Guest users are not allowed to view an arbitrary ticket"
            );
        }

        return new ResponseWrapperSingle<Ticket>(
            repository.findById(id).orElseThrow(
                () -> new TicketNotFoundException(id)
            )
        );
    }

    @GetMapping("/tickets/mine")
    @ApiOperation(
        value = "retrieve my tickets",
        response = Ticket.class,
        responseContainer = "List"
    )
    ResponseWrapper<Ticket> allOfMine(
        @RequestHeader HttpHeaders ppHeaders,
        @RequestParam(value = "screening_id", required = false)
        String screeningId
    ) {
        Claims allClaims = new DefaultAuthHeader(ppHeaders).token().claims();

        if (allClaims.perms().forbid(new SimpleOperation("TICKET.READ"))) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on TICKET[.MINE], " +
                "or your token is not valid"
            );
        }

        if (
            !allClaims.requester().roles().sameAs(new SimpleRole("ROLE_GUEST"))
        ) {
            throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Only guest users have their own tickets to access"
            );
        }

        try {
            List<Ticket> unwrapped = repository.findByUserAndScreening(
                allClaims.requester().id(),
                screeningId != null ?
                Long.valueOf(screeningId) :
                Long.valueOf(0)
            );
            return new ResponseWrapper<Ticket>(unwrapped);
        }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Malformed query param value: 'screening_id'"
            );
        }
    }

    @GetMapping("/tickets/mine/{id}")
    ResponseWrapperSingle<Ticket> oneOfMine(
        @RequestHeader HttpHeaders ppHeaders,
        @PathVariable Long id
    ) {
        Claims allClaims = new DefaultAuthHeader(ppHeaders).token().claims();

        if (allClaims.perms().forbid(new SimpleOperation("TICKET.READ"))) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on TICKET[.MINE], " +
                "or your token is not valid"
            );
        }

        if (
            !allClaims.requester().roles().sameAs(new SimpleRole("ROLE_GUEST"))
        ) {
            throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Only guest users have their own tickets to access"
            );
        }

        Ticket onlyTicket = repository.findById(id).orElseThrow(
            () -> new TicketNotFoundException(id)
        );

        if (!onlyTicket.getBooking().getUserId().equals(
            allClaims.requester().id())
        ) {
            throw new TicketNotFoundException(id);
        }
        return new ResponseWrapperSingle<Ticket>(onlyTicket);
    }
}
