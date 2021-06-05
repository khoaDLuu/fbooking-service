package com.filmbooking.booking_service.reqres;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class PaypalRequest {

    private Long userId;
    private String currency;
    private List<TicketDetails> tickets;

    public PaypalRequest(
        Long userId,
        String currency,
        List<TicketDetails> tickets
    ) {
        this.userId = userId;
        this.currency = currency;
        this.tickets = tickets;
    }

    public BigDecimal total() {
        BigDecimal sum = BigDecimal.valueOf(0);
        if (tickets == null) {
            return new BigDecimal(0);
        }
        for (TicketDetails ticketDetails : tickets) {
            sum = sum.add(ticketDetails.getPrice());
        }
        return sum;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<TicketDetails> getTickets() {
        return this.tickets;
    }

    public void setTickets(List<TicketDetails> tickets) {
        this.tickets = tickets;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (!(o instanceof PaypalRequest))
            return false;

        PaypalRequest ppReq = (PaypalRequest) o;
        return Objects.equals(this.userId, ppReq.userId)
            && Objects.equals(this.currency, ppReq.currency)
            && Objects.equals(this.tickets, ppReq.tickets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.userId, this.currency, this.tickets);
    }

    @Override
    public String toString() {
        return "PaypalRequest{" +
            "userId='" + this.userId + "\', " +
            "currency='" + this.currency + "\', " +
            "tickets='" + this.tickets + '}';
    }
}

class TicketDetails {
    private String seatNumber;
    private BigDecimal price;

    public TicketDetails(String seatNumber, BigDecimal price) {
        this.seatNumber = seatNumber;
        this.price = price;
    }

    public String getSeatNumber() {
        return this.seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (!(o instanceof TicketDetails))
            return false;

        TicketDetails ticDetails = (TicketDetails) o;
        return Objects.equals(this.seatNumber, ticDetails.seatNumber)
            && Objects.equals(this.price, ticDetails.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.seatNumber, this.price);
    }

    @Override
    public String toString() {
        return "TicketDetails{" +
            "seatNumber='" + this.seatNumber + "\', " +
            "price='" + this.price + '}';
    }
}
