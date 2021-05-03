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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.filmbooking.booking_service.models.Booking;

@RestController
class BookingController {

    @Autowired
    private PaypalClient paypal;
    private final BookingRepository repository;

    BookingController(BookingRepository repository) {
        this.repository = repository;
    }

    @PostMapping("api/v1/order")
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
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Status: " + response.result().status());
            System.out.println("Order ID: " + response.result().id());
            System.out.println("Intent: " + response.result().checkoutPaymentIntent());
            System.out.println("Links: ");
            for (LinkDescription link : response.result().links()) {
                System.out.println("\t" + link.rel() + ": " + link.href() + "\tCall Type: " + link.method());
            }
            System.out.println(
                    "Total Amount: " + response.result().purchaseUnits().get(0).amountWithBreakdown().currencyCode()
                            + " " + response.result().purchaseUnits().get(0).amountWithBreakdown().value());
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

    @PostMapping("api/v1/approve")
    Booking approveOrder(@RequestBody Booking booking) throws IOException {

        System.out.println(booking.getOrderId());
        System.out.println(booking.getPayerId());

        // PaypalResponse res = new PaypalResponse();
        OrdersGetRequest request = new OrdersGetRequest(booking.getOrderId());

        // Call PayPal to get the transaction
        HttpResponse<Order> response = paypal.client().execute(request);

        // Save the transaction in your database.
        // Implement logic to save transaction to
        // your database for future reference.
        if (response.statusCode() == HttpStatus.OK.value()) {
            response = this.captureOrder(booking);

            if (response.statusCode() == HttpStatus.CREATED.value()) {
                // System.out.println(new JSONObject(new Json().serialize(response.result())));
                System.out.println("Order ID: " + response.result().id());
                return booking;
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    @Transactional
    public HttpResponse<Order> captureOrder(Booking booking) throws IOException {
        OrdersCaptureRequest request = new OrdersCaptureRequest(booking.getOrderId());
        request.requestBody(buildRequestBody());
        // Call PayPal to capture an order
        HttpResponse<Order> response = paypal.client().execute(request);
        // Save the capture ID to your database. Implement logic to save capture to your
        // database for future reference.
        if (response.statusCode() == HttpStatus.CREATED.value()) {
            for (PurchaseUnit purchaseUnit : response.result().purchaseUnits()) {
                purchaseUnit.amountWithBreakdown();
                for (Capture capture : purchaseUnit.payments().captures()) {
                    System.out.println(Double.parseDouble(capture.amount().value()));
                    System.out.println(capture.id());
                    // Here you can insert order into database
                }
            }
            booking.getTickets().forEach(ticket -> ticket.setBooking(booking));
            repository.save(booking);
            return response;
        }

    /**
     * Creating empty body for capture request. You can set the payment source if
     * required.
     *
     * @return OrderRequest request with empty body
     */
    private OrderRequest buildRequestBody() {
        return new OrderRequest();
    }

    private OrderRequest buildRequestBody(
        BigDecimal amount,
        String currency
    ) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        ApplicationContext applicationContext = new ApplicationContext().brandName("FILMBOOKING")
                .landingPage("BILLING");
        orderRequest.applicationContext(applicationContext);

        List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<PurchaseUnitRequest>();
        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest().referenceId("PUHF")
                .description("Film Tickets").customId("CUST-FB").softDescriptor("FB")
                .amountWithBreakdown(new AmountWithBreakdown().currencyCode("USD").value(amount.toString()));
        purchaseUnitRequests.add(purchaseUnitRequest);
        orderRequest.purchaseUnits(purchaseUnitRequests);
        return orderRequest;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("/bookings")
    List<Booking> all() {
        return repository.findAll();
    }
    // end::get-aggregate-root[]

    @PostMapping("/bookings")
    Booking newBooking(@RequestBody Booking newBooking) {
        return repository.save(newBooking);
    }

    // Single item

    @GetMapping("/bookings/{id}")
    Booking one(@PathVariable Long id) {

        return repository.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
    }

    @PutMapping("/bookings/{id}")
    Booking replaceBooking(@RequestBody Booking newBooking, @PathVariable Long id) {

        return repository.findById(id).map(booking -> {
            booking.setUserId(newBooking.getUserId());
            booking.setCurrency(newBooking.getCurrency());
            booking.setAmount(newBooking.getAmount());
            booking.setCreatedAt(newBooking.getCreatedAt());
            return repository.save(booking);
        }).orElseGet(() -> {
            newBooking.setId(id);
            return repository.save(newBooking);
        });
    }

    @DeleteMapping("/bookings/{id}")
    void deleteBooking(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
