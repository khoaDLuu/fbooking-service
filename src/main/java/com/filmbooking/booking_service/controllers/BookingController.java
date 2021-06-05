package com.filmbooking.booking_service.controllers;

import java.io.IOException;
import java.util.List;

import com.paypal.http.HttpResponse;
import com.paypal.orders.Order;

import org.postgresql.util.PSQLException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.ApiOperation;

import com.filmbooking.booking_service.errors_handling.PaypalTransactionException;
import com.filmbooking.booking_service.errors_handling.QrSendingFailure;
import com.filmbooking.booking_service.models.Booking;
import com.filmbooking.booking_service.repositories.BookingRepository;
import com.filmbooking.booking_service.reqres.PaypalRequest;
import com.filmbooking.booking_service.reqres.PaypalResponse;
import com.filmbooking.booking_service.reqres.ResponseWrapper;
import com.filmbooking.booking_service.reqres.ResponseWrapperSingle;
import com.filmbooking.booking_service.services.PaypalService;
import com.filmbooking.booking_service.services.QrSender;
import com.filmbooking.booking_service.utils.authHeader.AuthHeader;
import com.filmbooking.booking_service.utils.authHeader.DefaultAuthHeader;
import com.filmbooking.booking_service.utils.permission.operation.Operation;
import com.filmbooking.booking_service.utils.permission.operation.SimpleOperation;
import com.filmbooking.booking_service.utils.token.claims.Claims;
import com.filmbooking.booking_service.utils.user.role.SimpleRole;

@RestController
class BookingController {

    private final BookingRepository repository;

    BookingController(BookingRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/bookings/prepare")
    PaypalResponse prepareOrder(
        @RequestBody PaypalRequest ppReq,
        @RequestHeader HttpHeaders ppHeaders
    ) {
        AuthHeader auth = new DefaultAuthHeader(ppHeaders);
        Operation thisOp = new SimpleOperation("BOOKING.CREATE");
        if (!auth.token().claims().perms().allow(thisOp)) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "CREATE action on BOOKING, " +
                "or your token is not valid"
            );
        }

        try {
            HttpResponse<Order> resp = new PaypalService().createOrder(ppReq);
            return new PaypalResponse(
                resp.result().id(),
                resp.result().purchaseUnits().get(0)
                    .amountWithBreakdown().value() + " " +
                    resp.result().purchaseUnits().get(0)
                        .amountWithBreakdown().currencyCode(),
                        resp.result().createTime()
            );
        }
        catch (PaypalTransactionException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage()
            );
        }
        catch (IOException ex) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to make a request to PayPal to set up the transaction"
            );
        }
    }

    @PostMapping("/bookings/confirm")
    Booking confirmOrder(
        @RequestBody Booking booking,
        @RequestHeader HttpHeaders ppHeaders
    ) {
        AuthHeader auth = new DefaultAuthHeader(ppHeaders);
        Operation thisOp = new SimpleOperation("BOOKING.CREATE");
        if (!auth.token().claims().perms().allow(thisOp)) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "CREATE action on BOOKING, " +
                "or your token is not valid"
            );
        }

        try {
            Booking verifiedBooking = new PaypalService().approveOrder(booking);
            verifiedBooking.getTickets().forEach(
                ticket -> ticket.setBooking(booking)
            );
            try {
                repository.save(booking);
            }
            catch (Exception ex) {
                throw new PSQLException(
                    ((PSQLException) ex).getServerErrorMessage(),
                    true
                );
            }
            new QrSender().sendCodeForQR(
                booking.getCode(),
                ppHeaders.getOrEmpty("Authorization").get(0),
                booking.getUserEmail()
            );
            return verifiedBooking;
        }
        catch (PaypalTransactionException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage()
            );
        }
        catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to make a request to PayPal to get/capture " +
                "the transaction"
            );
        }
        catch (PSQLException sqlEx) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                sqlEx.getMessage()
            );
        }
        catch (QrSendingFailure e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage()
            );
        }
    }

    @GetMapping("/bookings")
    @ApiOperation(
        value = "retrieve bookings (optionally by user_id)",
        response = Booking.class,
        responseContainer = "List"
    )
    ResponseWrapper<Booking> all(
        @RequestHeader HttpHeaders ppHeaders,
        @RequestParam(value = "user_id", required = false) String userId
    ) {
        Claims allClaims = new DefaultAuthHeader(ppHeaders).token().claims();

        if (allClaims.perms().forbid(new SimpleOperation("BOOKING.READ"))) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on BOOKING, " +
                "or your token is not valid"
            );
        }

        if (allClaims.requester().roles().sameAs(new SimpleRole("ROLE_GUEST"))) {
            throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Guest users are not allowed to view all bookings"
            );
        }

        List<Booking> unwrapped = null;
        if (userId != null) {
            unwrapped = repository.findByUser(Long.parseLong(userId));
        }
        else {
            unwrapped = repository.findAll();
        }
        return new ResponseWrapper<Booking>(unwrapped);
    }

    @GetMapping("/bookings/by-code/{code}")
    @ApiOperation(
        value = "retrieve bookings by code",
        response = Booking.class
    )
    ResponseWrapperSingle<Booking> oneByCode(
        @RequestHeader HttpHeaders ppHeaders,
        @PathVariable String code
    ) {
        Claims allClaims = new DefaultAuthHeader(ppHeaders).token().claims();

        if (allClaims.perms().forbid(new SimpleOperation("BOOKING.READ"))) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on BOOKING, " +
                "or your token is not valid"
            );
        }

        try {
            Booking unwrapped = repository.findByCode(code);
            return new ResponseWrapperSingle<Booking>(unwrapped);
        }
        catch (IndexOutOfBoundsException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Your booking code is invalid or missing"
            );
        }
    }

    @GetMapping("/bookings/{id}")
    ResponseWrapperSingle<Booking> one(@RequestHeader HttpHeaders ppHeaders, @PathVariable Long id) {
        Claims allClaims = new DefaultAuthHeader(ppHeaders).token().claims();

        if (allClaims.perms().forbid(new SimpleOperation("BOOKING.READ"))) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on BOOKING, " +
                "or your token is not valid"
            );
        }

        if (allClaims.requester().roles().sameAs(new SimpleRole("ROLE_GUEST"))) {
            throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Guest users are not allowed to view an arbitrary booking"
            );
        }

        return new ResponseWrapperSingle<Booking>(
            repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Booking not found, booking id = " + id
                )
            )
        );
    }

    @GetMapping("/bookings/mine")
    @ApiOperation(
        value = "retrieve my bookings",
        response = Booking.class,
        responseContainer = "List"
    )
    ResponseWrapper<Booking> allOfMine(@RequestHeader HttpHeaders ppHeaders) {
        Claims allClaims = new DefaultAuthHeader(ppHeaders).token().claims();

        if (allClaims.perms().forbid(new SimpleOperation("BOOKING.READ"))) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on BOOKING[.MINE], " +
                "or your token is not valid"
            );
        }

        if (!allClaims.requester().roles().sameAs(new SimpleRole("ROLE_GUEST"))) {
            throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Only guest users have their own bookings to access"
            );
        }

        List<Booking> unwrapped = null;
        unwrapped = repository.findByUser(allClaims.requester().id());
        return new ResponseWrapper<Booking>(unwrapped);
    }

    @GetMapping("/bookings/mine/{id}")
    ResponseWrapperSingle<Booking> oneOfMine(@RequestHeader HttpHeaders ppHeaders, @PathVariable Long id) {
        Claims allClaims = new DefaultAuthHeader(ppHeaders).token().claims();

        if (allClaims.perms().forbid(new SimpleOperation("BOOKING.READ"))) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on BOOKING[.MINE], " +
                "or your token is not valid"
            );
        }

        if (!allClaims.requester().roles().sameAs(new SimpleRole("ROLE_GUEST"))) {
            throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Only guest users have their own bookings to access"
            );
        }

        Booking onlyBooking = repository.findById(id).orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Booking not found, booking id = " + id
            )
        );
        if (!onlyBooking.getUserId().equals(allClaims.requester().id())) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Booking not found, booking id = " + id +
                ", user id = " + allClaims.requester().id()
            );
        }
        return new ResponseWrapperSingle<Booking>(onlyBooking);
    }

    @PutMapping("/bookings/{id}")
    Booking replaceBooking(
        @RequestHeader HttpHeaders ppHeaders,
        @RequestBody Booking newBooking,
        @PathVariable Long id
    ) {
        if (new DefaultAuthHeader(ppHeaders).token().claims().perms().forbid(
            new SimpleOperation("BOOKING.UPDATE")
        )) {
            throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Currently, no one has the permission to perform " +
                "UPDATE action on BOOKING"
            );
        }

        // Note: Use case: when the user cancel a booking ?
        return repository.findById(id).map(booking -> {
            booking.setUserId(newBooking.getUserId());
            booking.setCurrency(newBooking.getCurrency());
            booking.setAmount(newBooking.getAmount());
            return repository.save(booking);
        }).orElseGet(() -> {
            // newBooking.setId(id);
            return repository.save(newBooking);
        });
    }

    @DeleteMapping("/bookings/{id}")
    void deleteBooking(
        @RequestHeader HttpHeaders ppHeaders,
        @PathVariable Long id
    ) {
        if (new DefaultAuthHeader(ppHeaders).token().claims().perms().forbid(
            new SimpleOperation("BOOKING.DELETE")
        )) {
            throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Currently, no one has the permission to perform " +
                "DELETE action on BOOKING"
            );
        }

        repository.deleteById(id);
    }
}
