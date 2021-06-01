package com.filmbooking.booking_service.controllers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.filmbooking.booking_service.ResponseWrapper;
import com.filmbooking.booking_service.ResponseWrapperSingle;
import com.filmbooking.booking_service.models.Revenue;
import com.filmbooking.booking_service.repositories.RevenueRepository;
import com.filmbooking.booking_service.utils.authHeader.DefaultAuthHeader;
import com.filmbooking.booking_service.utils.permission.operation.SimpleOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
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
    ResponseWrapper<Revenue> manyByDateRange(
        @RequestHeader HttpHeaders ppHeaders,
        @RequestParam(value = "from", required = false)
        String from,
        @RequestParam(value = "to", required = false)
        String to,
        @RequestParam(value = "movie_id", required = false)
        String _movieId
    ) {
        if (new DefaultAuthHeader(ppHeaders).token().claims().perms().forbid(
            new SimpleOperation("REVENUE.READ")
        )) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on REVENUE, " +
                "or your token is not valid"
            );
        }

        try {
            List<Revenue> unwrapped = null;

            LocalDate dateFrom = from == null ? LocalDate.of(1970, 1, 1)
                               : LocalDate.parse(from);
            LocalDate dateTo = to == null ? LocalDate.now(ZoneId.systemDefault())
                             : LocalDate.parse(to);

            if (_movieId == null) {
                unwrapped = repo.retrieveForAll(
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
                "Malformed query param value: 'from' or 'to'"
            );
        }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Malformed query param value: 'movie_id'"
            );
        }
    }

    @GetMapping("/revenues/total/by-movie/{movieId}")
    @ApiOperation(
        value = "retrieve total revenue for a movie",
        response = Revenue.class
    )
    ResponseWrapperSingle<Revenue> manyByMovie(
        @RequestHeader HttpHeaders ppHeaders,
        @PathVariable Long movieId
    ) {
        if (new DefaultAuthHeader(ppHeaders).token().claims().perms().forbid(
            new SimpleOperation("REVENUE.READ")
        )) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on REVENUE, " +
                "or your token is not valid"
            );
        }

        Revenue totalRev = repo.retrieveTotalForOne(movieId);
        return new ResponseWrapperSingle<Revenue>(totalRev);
    }

    @GetMapping("/revenues/total/by-movie/latest")
    @ApiOperation(
        value = "retrieve revenues for latest movies",
        response = Revenue.class,
        responseContainer = "List"
    )
    ResponseWrapper<Revenue> manyByMovie(
        @RequestHeader HttpHeaders ppHeaders,
        @RequestParam(value = "movie_count", required = false)
        String _movieCount
    ) {
        if (new DefaultAuthHeader(ppHeaders).token().claims().perms().forbid(
            new SimpleOperation("REVENUE.READ")
        )) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You don't have the permission to perform " +
                "READ action on REVENUE, " +
                "or your token is not valid"
            );
        }

        try {
            List<Revenue> unwrapped = null;
            Long movieCount = _movieCount == null ? 5
                            : Long.valueOf(_movieCount);
            unwrapped = repo.retrieveTotalForLatest(movieCount);
            return new ResponseWrapper<Revenue>(unwrapped);
        }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Malformed query param value: 'movie_count'"
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
