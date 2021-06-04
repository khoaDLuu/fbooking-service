package com.filmbooking.booking_service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.paypal.http.HttpResponse;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.Capture;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.OrdersGetRequest;
import com.paypal.orders.PurchaseUnit;
import com.paypal.orders.PurchaseUnitRequest;

import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.ApiOperation;

import com.filmbooking.booking_service.models.Booking;
import com.filmbooking.booking_service.repositories.BookingRepository;
import com.filmbooking.booking_service.utils.authHeader.AuthHeader;
import com.filmbooking.booking_service.utils.authHeader.DefaultAuthHeader;
import com.filmbooking.booking_service.utils.permission.operation.Operation;
import com.filmbooking.booking_service.utils.permission.operation.SimpleOperation;
import com.filmbooking.booking_service.utils.token.claims.Claims;
import com.filmbooking.booking_service.utils.user.role.SimpleRole;

@RestController
class BookingController {

    @Autowired
    private PaypalClient paypal;
    private final BookingRepository repository;

    BookingController(BookingRepository repository) {
        this.repository = repository;
    }

    private OrderRequest buildRequestBody() {
        return new OrderRequest();
    }

    private OrderRequest buildRequestBody(
        BigDecimal amount,
        String currency
    ) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        ApplicationContext applicationContext = new ApplicationContext()
            .brandName("FILMBOOKING")
            .landingPage("BILLING");
        orderRequest.applicationContext(applicationContext);

        List<PurchaseUnitRequest> purchaseUnitRequests =
            new ArrayList<PurchaseUnitRequest>();
        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
            .referenceId("PUHF")
            .description("Film Tickets")
            .softDescriptor("FBT")
            .amountWithBreakdown(
                new AmountWithBreakdown()
                .currencyCode(currency)
                .value(amount.toString())
            );
        purchaseUnitRequests.add(purchaseUnitRequest);
        orderRequest.purchaseUnits(purchaseUnitRequests);
        return orderRequest;
    }

    @PostMapping("/bookings/prepare")
    PaypalResponse createOrder(
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

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");
        request.requestBody(buildRequestBody(
            ppReq.total(),
            ppReq.getCurrency()
        ));

        try {
            // Call PayPal to set up a transaction
            HttpResponse<Order> resp = this.paypal.client().execute(request);

            if (resp.statusCode() == HttpStatus.CREATED.value()) {
                // ############## DEBUG ############## //
                System.out.println("Status Code: " + resp.statusCode());
                System.out.println("Status: " + resp.result().status());
                System.out.println("Order ID: " + resp.result().id());
                System.out.println(
                    "Intent: " + resp.result().checkoutPaymentIntent()
                );
                System.out.println("Links: ");
                for (LinkDescription link : resp.result().links()) {
                    System.out.println(
                        "\t" + link.rel() +
                        ": " + link.href() +
                        "\tCall Type: " + link.method()
                    );
                }
                System.out.println(
                    "Total Amount: "
                    + resp.result().purchaseUnits().get(0)
                        .amountWithBreakdown().currencyCode() + " " +
                        resp.result().purchaseUnits().get(0)
                        .amountWithBreakdown().value()
                );
                // ############## DEBUG ############## //
            }
            else {
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to set up Paypal transaction, code " +
                    resp.statusCode()
                );
            }

            return new PaypalResponse(
                resp.result().id(),
                resp.result().purchaseUnits().get(0)
                    .amountWithBreakdown().value() + " " +
                    resp.result().purchaseUnits().get(0)
                        .amountWithBreakdown().currencyCode(),
                        resp.result().createTime()
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
    Booking approveOrder(
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

        OrdersGetRequest request = new OrdersGetRequest(booking.getOrderId());

        try {
            // Call PayPal to get the transaction
            HttpResponse<Order> response = paypal.client().execute(request);

            if (response.statusCode() == HttpStatus.OK.value()) {
                response = this.captureOrder(booking);

                if (response.statusCode() == HttpStatus.CREATED.value()) {
                    // ############## DEBUG ############## //
                    System.out.println("Order ID: " + response.result().id());
                    // ############## DEBUG ############## //

                    this.sendCodeForQR(
                        booking.getCode(),
                        ppHeaders.getOrEmpty("Authorization").get(0),
                        booking.getUserEmail()
                    );

                    return booking;
                }
                else {
                    throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to capture Paypal order, code " +
                        response.statusCode()
                    );
                }
            }
            else {
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve Paypal transaction, code " +
                    response.statusCode()
                );
            }
        }
        catch (IOException ex) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to make a request to PayPal to get/capture " +
                "the transaction"
            );
        }
    }

    private void sendCodeForQR(String code, String auth, String emailTo) {
        WebClient apiClient = WebClient.create(
            System.getenv("QR_MAIL_URL_BASE")
        );
        String bodyContent = String.format(
            "{\"mailFrom\":\"%s\"," +
            "\"mailTo\":\"%s\"," +
            "\"embeddedlink\":\"%s?code=%s\"}",
            System.getenv("EMAIL_DEFAULT"), emailTo,
            System.getenv("MANAGEMENT_SITE_URL"), code
        );

        // ############## DEBUG ############## //
        System.out.println("Body content: " + bodyContent);
        // ############## DEBUG ############## //

        String res = apiClient.post()
            .uri(System.getenv("QR_MAIL_URL_PATH"))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", auth)
            .body(BodyInserters.fromValue(bodyContent))
            .retrieve()
            .bodyToMono(String.class)
            .doOnError(throwable ->
                System.out.println("Failed for some reason: " + throwable))
            .onErrorReturn(new String(""))
            .block();
        System.out.println("QR sending result: " + res);

        if (res.equals("")) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to send QR code to mailing service"
            );
        }
    }

    @Transactional
    public HttpResponse<Order> captureOrder(Booking booking) throws IOException {
        try {
            OrdersCaptureRequest request = new OrdersCaptureRequest(booking.getOrderId());
            request.requestBody(buildRequestBody());

            // Call PayPal to capture an order
            HttpResponse<Order> response = paypal.client().execute(request);

            if (response.statusCode() == HttpStatus.CREATED.value()) {
                for (PurchaseUnit purchaseUnit : response.result().purchaseUnits()) {
                    purchaseUnit.amountWithBreakdown();
                    for (Capture capture : purchaseUnit.payments().captures()) {
                        // ############## DEBUG ############## //
                        System.out.println(
                            "CaptureOrder - id: " +
                            capture.id()
                        );
                        System.out.println(
                            "CaptureOrder - amount: " +
                            Double.parseDouble(capture.amount().value())
                        );
                        // ############## DEBUG ############## //
                    }
                }
                booking.getTickets().forEach(ticket -> ticket.setBooking(booking));
                try {
                    repository.save(booking);
                }
                catch (Exception ex) {
                    throw new PSQLException(
                        ((PSQLException) ex).getServerErrorMessage(),
                        true
                    );
                }
            }
            return response;
        }
        catch (PSQLException sqlEx) {
            throw new ConstraintViolationException(sqlEx.getMessage());
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
                () -> new BookingNotFoundException(id)
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
            () -> new BookingNotFoundException(id)
        );
        if (!onlyBooking.getUserId().equals(allClaims.requester().id())) {
            throw new BookingNotFoundException(id);
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
