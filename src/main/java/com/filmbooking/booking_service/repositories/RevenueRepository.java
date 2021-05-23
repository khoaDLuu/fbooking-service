package com.filmbooking.booking_service.repositories;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;

import com.filmbooking.booking_service.models.Revenue;

@Component
public class RevenueRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<Revenue> retrieveBetween(
        Instant dateFrom, Instant dateTo
    ) {
        String hql = "SELECT cast(e.createdAt as date) as _date, " +
                     "SUM(e.amount) as _total " +
                     "FROM Booking e " +
                     "WHERE (e.createdAt BETWEEN :dateFrom AND :dateTo) " +
                     "GROUP BY _date";
        Query query = entityManager.createQuery(hql);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        List<Object[]> revenue = (List<Object[]>) query.getResultList();
        return revenue
            .stream()
            .map(row -> new Revenue(
                    LocalDate.parse(row[0].toString()),
                    new BigDecimal(row[1].toString())
                )
            )
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<Revenue> retrieveForMovie(
        Instant dateFrom, Instant dateTo,
        Long movieId
    ) {
        String hql = "SELECT cast(e.createdAt as date) as _date, " +
                     "SUM(e.amount) as _total " +
                     "FROM Booking e " +
                     "WHERE e.movieId = :movieId " +
                     "AND (e.createdAt BETWEEN :dateFrom AND :dateTo) " +
                     "GROUP BY _date";
        Query query = entityManager.createQuery(hql);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        query.setParameter("movieId", movieId);
        List<Object[]> revenue = (List<Object[]>) query.getResultList();
        return revenue
            .stream()
            .map(row -> new Revenue(
                    LocalDate.parse(row[0].toString()),
                    new BigDecimal(row[1].toString())
                )
            )
            .collect(Collectors.toList());
    }
}
