package com.filmbooking.booking_service.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "bookings")
public class Booking implements Serializable {

    private @Id @GeneratedValue Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "payer_id", nullable = false)
    private String payerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "currency")
    private String currency;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", updatable = false)
    private @CreatedDate Instant createdAt;

    @Column(name = "updated_at")
    private @LastModifiedDate Instant updatedAt;

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets;

    public Booking() {
    }

    public Booking(String orderId, String payerId, Long userId,
                   String currency, BigDecimal amount, List<Ticket> tickets) {
        this.orderId = orderId;
        this.payerId = payerId;
        this.userId = userId;
        this.currency = currency;
        this.amount = amount;
        this.tickets = tickets;
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
        return this.payerId;
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

    public List<Ticket> getTickets() {
        return this.tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void setupdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
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
            && Objects.equals(this.createdAt, booking.createdAt)
            && Objects.equals(this.updatedAt, booking.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.id,
            this.orderId, this.payerId,
            this.userId, this.currency, this.amount,
            this.createdAt, this.updatedAt
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
            "createdAt=" + this.createdAt + "\', " +
            "updatedAt='" + this.updatedAt + '}';
    }
}
