package com.filmbooking.booking_service.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.filmbooking.booking_service.errors_handling.PaypalTransactionException;
import com.filmbooking.booking_service.models.Booking;
import com.filmbooking.booking_service.reqres.PaypalRequest;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaypalService {

    @Autowired
    private PaypalClient paypal;

    public HttpResponse<Order> createOrder(PaypalRequest ppReq)
    throws IOException, PaypalTransactionException {
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");
        request.requestBody(this.buildRequestBody(
            ppReq.total(),
            ppReq.getCurrency()
        ));

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
            return resp;
        }
        else {
            throw new PaypalTransactionException(
                "Failed to set up Paypal transaction, status " +
                resp.statusCode()
            );
        }
    }

    public Booking approveOrder(Booking booking) throws IOException, PaypalTransactionException {
        OrdersGetRequest request = new OrdersGetRequest(booking.getOrderId());

        // Call PayPal to get the transaction
        HttpResponse<Order> respGet = paypal.client().execute(request);

        if (respGet.statusCode() == HttpStatus.OK.value()) {
            HttpResponse<Order> respCap = this.captureOrder(booking);
            // ############## DEBUG ############## //
            System.out.println("Order ID: " + respCap.result().id());
            // ############## DEBUG ############## //
            return booking;
        }
        else {
            throw new PaypalTransactionException(
                "Failed to retrieve Paypal transaction, status " +
                respGet.statusCode()
            );
        }
    }

    @Transactional
    private HttpResponse<Order> captureOrder(Booking booking)
    throws IOException, PaypalTransactionException {
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
            return response;
        }
        else {
            throw new PaypalTransactionException(
                "Failed to capture Paypal order, status " +
                response.statusCode()
            );
        }
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

}
