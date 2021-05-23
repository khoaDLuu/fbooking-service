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

    public Revenue(LocalDate date, BigDecimal total) {
        this.date = date;
        this.total = total;
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

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (!(o instanceof Revenue))
            return false;

        Revenue rev = (Revenue) o;
        return Objects.equals(this.date, rev.date)
            && Objects.equals(this.total, rev.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.date, this.total);
    }

    @Override
    public String toString() {
        return "Revenue{" +
            "date=" + this.date + ", " +
            "total=" + this.total + " USD}";
    }
}
