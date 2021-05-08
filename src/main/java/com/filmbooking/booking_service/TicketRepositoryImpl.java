package com.filmbooking.booking_service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
}
