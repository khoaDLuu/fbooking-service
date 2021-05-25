package com.filmbooking.booking_service.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Revenue {
    /* TODO
     * Fix currency issue
     * Currently, USD is the only valid currency
     */

    LocalDate date;
    BigDecimal total;
    Long movieId;

    public Revenue(LocalDate date, BigDecimal total, Long movieId) {
        this.date = date;
        this.total = total;
        this.movieId = movieId;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Long getMovieId() {
        return this.movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (!(o instanceof Revenue))
            return false;

        Revenue rev = (Revenue) o;
        return Objects.equals(this.date, rev.date)
            && Objects.equals(this.total, rev.total)
            && Objects.equals(this.movieId, rev.movieId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.date, this.total, this.movieId);
    }

    @Override
    public String toString() {
        return "Revenue{" +
            "date=" + this.date + ", " +
            "total=" + this.total + " USD, " +
            "movieId=" + this.movieId + "}";
    }
}
