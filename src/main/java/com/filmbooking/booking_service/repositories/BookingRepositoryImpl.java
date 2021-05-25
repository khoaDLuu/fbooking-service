package com.filmbooking.booking_service.repositories;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;

import com.filmbooking.booking_service.models.Booking;

@Component
public class BookingRepositoryImpl {
    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unused")
    public List<Booking> findByUser(Long userId) {
        String hql = "SELECT e FROM Booking e WHERE e.userId = :usr_id";
        TypedQuery<Booking> query = entityManager.createQuery(
            hql,
            Booking.class
        );
        query.setParameter("usr_id", userId);
        return query.getResultList();
    }
}
