package com.filmbooking.booking_service.controllers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.filmbooking.booking_service.ResponseWrapper;
import com.filmbooking.booking_service.models.Revenue;
import com.filmbooking.booking_service.repositories.RevenueRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
        @RequestParam(value = "from", required = true)
        String from,
        @RequestParam(value = "to", required = true)
        String to,
        @RequestParam(value = "movie_id", required = false)
        String _movieId
    ) {
        try {
            List<Revenue> unwrapped = null;

            LocalDate dateFrom = LocalDate.parse(from);
            LocalDate dateTo = LocalDate.parse(to);

            if (_movieId == null) {
                unwrapped = repo.retrieveBetween(
                    this.toTimestamp(dateFrom, true),
                    this.toTimestamp(dateTo, false)
                );
            }
            else {
                Long movieId = Long.valueOf(_movieId);

                unwrapped = repo.retrieveForMovie(
                    this.toTimestamp(dateFrom, true),
                    this.toTimestamp(dateTo, false),
                    movieId
                );
            }
            return new ResponseWrapper<Revenue>(unwrapped);
        }
        catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Malformed query params: 'from' or 'to'"
            );
        }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Malformed query params: 'movie_id'"
            );
        }
    }

    private Instant toTimestamp(LocalDate d, boolean startOrEndOfDay) {
        int days = startOrEndOfDay ? 0 : 1;
        return d
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .plus(days, ChronoUnit.DAYS);
    }

}
