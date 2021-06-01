package com.filmbooking.booking_service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import io.swagger.annotations.ApiOperation;

import com.filmbooking.booking_service.models.Booking;
import com.filmbooking.booking_service.repositories.BookingRepository;

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
    PaypalResponse createOrder(@RequestBody PaypalRequest ppReq) throws IOException {

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");
        request.requestBody(buildRequestBody(
            ppReq.total(),
            ppReq.getCurrency()
        ));

        // Call PayPal to set up a transaction
        HttpResponse<Order> response = this.paypal.client().execute(request);

        if (response.statusCode() == HttpStatus.CREATED.value()) {
            // ############## DEBUG ############## //
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Status: " + response.result().status());
            System.out.println("Order ID: " + response.result().id());
            System.out.println(
                "Intent: " + response.result().checkoutPaymentIntent()
            );
            System.out.println("Links: ");
            for (LinkDescription link : response.result().links()) {
                System.out.println(
                    "\t" + link.rel() +
                    ": " + link.href() +
                    "\tCall Type: " + link.method()
                );
            }
            System.out.println(
                "Total Amount: "
                + response.result().purchaseUnits().get(0)
                    .amountWithBreakdown().currencyCode() + " " +
                response.result().purchaseUnits().get(0)
                    .amountWithBreakdown().value()
            );
            // ############## DEBUG ############## //
        }

        return new PaypalResponse(
            response.result().id(),
            response.result().purchaseUnits().get(0)
                .amountWithBreakdown().value() + " " +
                response.result().purchaseUnits().get(0)
                    .amountWithBreakdown().currencyCode(),
            response.result().createTime()
        );
    }

    @PostMapping("/bookings/confirm")
    Booking approveOrder(
        @RequestBody Booking booking,
        HttpServletRequest req
    ) throws IOException {
        String authHeader = req.getHeader("Authorization");
        OrdersGetRequest request = new OrdersGetRequest(booking.getOrderId());

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
                    authHeader,
                    booking.getUserEmail()
                );

                return booking;
            }
            else {
                return null; // TODO : throw Exception
            }
        }
        else {
            return null; // TODO : throw Exception
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
            .block();
        System.out.println("Result here: " + res);
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
        value = "retrieve bookings",
        response = Booking.class,
        responseContainer = "List"
    )
    ResponseWrapper<Booking> all(
        @RequestParam(value = "user_id", required = false)
        String userId
    ) {
        List<Booking> unwrapped = null;
        if (userId != null) {
            unwrapped = repository.findByUser(Long.parseLong(userId));
        }
        else {
            unwrapped = repository.findAll();
        }
        return new ResponseWrapper<Booking>(unwrapped);
    }

    @GetMapping("/bookings/{id}")
    Booking one(@PathVariable Long id) {

        return repository.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
    }

    @PutMapping("/bookings/{id}")
    Booking replaceBooking(@RequestBody Booking newBooking, @PathVariable Long id) {
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
    void deleteBooking(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
