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
    public List<Revenue> retrieveForAll(
        Instant dateFrom, Instant dateTo
    ) {
        String hql = "SELECT cast(e.createdAt as date) as _date, " +
                     "e.movieId as _movie_id, " +
                     "SUM(e.amount) as _total " +
                     "FROM Booking e " +
                     "WHERE (e.createdAt BETWEEN :dateFrom AND :dateTo) " +
                     "GROUP BY _date, _movie_id";
        Query query = entityManager.createQuery(hql);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        List<Object[]> revenues = (List<Object[]>) query.getResultList();
        return revenues
            .stream()
            .map(row -> new Revenue(
                    LocalDate.parse(row[0].toString()),
                    new BigDecimal(row[2].toString()),
                    Long.valueOf(row[1] != null ? row[1].toString() : "-1")
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
                    new BigDecimal(row[1].toString()),
                    movieId
                )
            )
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<Revenue> retrieveTotalForLatest(Long movieCount) {
        String sql = "SELECT NULL as _date, " +
                     "SUM(b.amount) as _total, " +
                     "movie_id " +
                     "FROM bookings b " +
                     "WHERE movie_id IN (" +
                     "  SELECT b1.movie_id FROM bookings b1 " +
                     "  GROUP BY b1.movie_id " +
                     "  ORDER BY MAX(b1.created_at) DESC " +
                     "  LIMIT :mv_c) " +
                     "GROUP BY (_date, movie_id)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("mv_c", movieCount);
        List<Object[]> revenues = (List<Object[]>) query.getResultList();
        return revenues
            .stream()
            .map(row -> new Revenue(
                    null,
                    new BigDecimal(row[1].toString()),
                    Long.valueOf(row[2].toString())
                )
            )
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public Revenue retrieveTotalForOne(Long movieId) {
        String sql = "SELECT NULL as _date, " +
                     "SUM(b.amount) as _total, " +
                     "movie_id " +
                     "FROM bookings b " +
                     "WHERE movie_id = :mv_id " +
                     "GROUP BY (_date, movie_id)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("mv_id", movieId);
        List<Object[]> revenue = (List<Object[]>) query.getResultList();
        if (revenue.size() == 0) {
            return new Revenue(
                null,
                new BigDecimal(0),
                movieId
            );
        }
        return new Revenue(
            null,
            new BigDecimal(revenue.get(0)[1].toString()),
            Long.valueOf(revenue.get(0)[2].toString())
        );
    }
}
