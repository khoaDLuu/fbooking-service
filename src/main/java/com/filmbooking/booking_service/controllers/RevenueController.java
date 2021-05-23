package com.filmbooking.booking_service.controllers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.filmbooking.booking_service.ResponseWrapper;
import com.filmbooking.booking_service.models.Revenue;
import com.filmbooking.booking_service.repositories.RevenueRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class RevenueController {
    @Autowired
    private RevenueRepository repo;

    @GetMapping("/revenues")
    @ApiOperation(
        value = "retrieve revenues",
        response = Revenue.class,
        responseContainer = "List"
    )
    ResponseWrapper<Revenue> many(
        @RequestParam(value = "from", required = false)
        String dateFrom,
        @RequestParam(value = "to", required = false)
        String dateTo,
        @RequestParam(value = "movie_id", required = false)
        String movieId
    ) {
        //############# DEBUG ##############//
        System.out.println("dateFrom: " + dateFrom);
        System.out.println("dateTo: " + dateTo);
        System.out.println("movieId: " + movieId);
        //############# DEBUG ##############//
        List<Revenue> unwrapped = null;
        if (movieId == null) {
            unwrapped = repo.retrieveBetween(
                this.toTimestamp(LocalDate.parse(dateFrom), true),
                this.toTimestamp(LocalDate.parse(dateTo), false)
            );
        }
        else {
            unwrapped = repo.retrieveForMovie(
                this.toTimestamp(LocalDate.parse(dateFrom), true),
                this.toTimestamp(LocalDate.parse(dateTo), false),
                Long.valueOf(movieId)
            );
        }

        return new ResponseWrapper<Revenue>(unwrapped);
    }

    private Instant toTimestamp(LocalDate d, boolean startOrEndOfDay) {
        int days = startOrEndOfDay ? 0 : 1;
        return d
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .plus(days, ChronoUnit.DAYS);
    }

}
