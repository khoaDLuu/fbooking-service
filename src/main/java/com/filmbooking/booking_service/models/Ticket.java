package com.filmbooking.booking_service.models;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(
    name = "tickets",
    uniqueConstraints={@UniqueConstraint(columnNames = {
        "seat_number", "screening_id"
    })}
)
public class Ticket implements Serializable {

    private @Id @GeneratedValue Long id;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "screening_id", nullable = false)
    private Long screeningId;

    @Column(name = "created_at", updatable = false)
    private @CreatedDate Instant createdAt;

    @Column(name = "updated_at")
    private @LastModifiedDate Instant updatedAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name ="booking_id", nullable = false)
    private Booking booking;

    public Ticket() {
    }

    public Ticket(String seatNumber, Long screeningId, Booking booking) {
        this.seatNumber = seatNumber;
        this.screeningId = screeningId;
        this.booking = booking;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return this.booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Long getScreeningId() {
        return this.screeningId;
    }

    public void setScreeningId(Long screeningId) {
        this.screeningId = screeningId;
    }

    public String getSeatNumber() {
        return this.seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
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

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (!(o instanceof Ticket))
            return false;

        Ticket ticket = (Ticket) o;
        return Objects.equals(this.id, ticket.id)
            && Objects.equals(this.booking, ticket.booking)
            && Objects.equals(this.screeningId, ticket.screeningId)
            && Objects.equals(this.seatNumber, ticket.seatNumber)
            && Objects.equals(this.createdAt, ticket.createdAt)
            && Objects.equals(this.updatedAt, ticket.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.id,
            this.booking, this.screeningId, this.seatNumber,
            this.createdAt, this.updatedAt
        );
    }

    @Override
    public String toString() {
        return "Ticket{" +
            "id=" + this.id + ", " +
            "booking=" + this.booking + ", " +
            "screeningId=" + this.screeningId + ", " +
            "seatNumber=\'" + this.seatNumber + "\', " +
            "createdAt=\'" + this.createdAt + "\', " +
            "updatedAt=\'" + this.updatedAt + "\'}";
    }
}
