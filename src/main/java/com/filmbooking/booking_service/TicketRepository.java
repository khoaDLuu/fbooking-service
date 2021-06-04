package com.filmbooking.booking_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.filmbooking.booking_service.models.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByScreening(Long screeningId);
    List<Ticket> findByUserAndScreening(Long userId, Long screeningId);
}
