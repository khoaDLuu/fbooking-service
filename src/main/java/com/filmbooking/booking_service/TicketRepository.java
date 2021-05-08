package com.filmbooking.booking_service;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.filmbooking.booking_service.models.Ticket;
import com.filmbooking.booking_service.models.Booking;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByBooking(Booking booking, Sort sort);
    List<Ticket> findByScreening(Long screeningId);
}
