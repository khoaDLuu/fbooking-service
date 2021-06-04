package com.filmbooking.booking_service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;

import com.filmbooking.booking_service.models.Ticket;

@Component
public class TicketRepositoryImpl {
    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unused")
    public List<Ticket> findByScreening(Long screeningId) {
        String hql = "SELECT e FROM Ticket e WHERE e.screeningId = :scr_id";
        TypedQuery<Ticket> query = entityManager.createQuery(
            hql,
            Ticket.class
        );
        query.setParameter("scr_id", screeningId);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
	public List<Ticket> findByUser(Long userId) {
        String sql = "SELECT t.id, t.seat_number, t.screening_id, " +
                     "t.created_at, t.updated_at " +
                     "FROM bookings b " +
                     "JOIN tickets t " +
                     "ON t.booking_id = b.id " +
                     "WHERE b.user_id = :usr_id";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("usr_id", userId);
        List<Object[]> tickets = (List<Object[]>) query.getResultList();
        return tickets
            .stream()
            .map(row -> new Ticket(
                    Long.valueOf(row[0].toString()),
                    row[1].toString(),
                    Long.valueOf(row[2].toString()),
                    row[3] != null ? Instant.parse(row[3].toString()) : null,
                    row[4] != null ? Instant.parse(row[4].toString()) : null
                )
            )
            .collect(Collectors.toList());
    }
}
