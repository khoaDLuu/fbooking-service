package com.filmbooking.booking_service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
class Booking {

    private @Id @GeneratedValue Long id;
    private String orderId;
    private String payerId;
    private Long userId;
    private String currency;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    Booking() {
    }

    Booking(String orderId, String payerId, Long userId, String currency, BigDecimal amount, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.payerId = payerId;
        this.userId = userId;
        this.currency = currency;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPayerId() {
        return this.orderId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
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

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (!(o instanceof Booking))
            return false;

        Booking booking = (Booking) o;
        return Objects.equals(this.id, booking.id)
            && Objects.equals(this.orderId, booking.orderId)
            && Objects.equals(this.payerId, booking.payerId)
            && Objects.equals(this.userId, booking.userId)
            && Objects.equals(this.currency, booking.currency)
            && Objects.equals(this.amount, booking.amount)
            && Objects.equals(this.createdAt, booking.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.id,
            this.orderId, this.payerId,
            this.userId, this.currency, this.amount, this.createdAt
        );
    }

    @Override
    public String toString() {
        return "Booking{" +
            "id=" + this.id + "\', " +
            "orderId=" + this.orderId + "\', " +
            "payerId=" + this.payerId + "\', " +
            "userId='" + this.userId + "\', " +
            "total=" + this.amount + " " + this.currency + "\', " +
            "createdAt='" + this.createdAt + '}';
    }
}
