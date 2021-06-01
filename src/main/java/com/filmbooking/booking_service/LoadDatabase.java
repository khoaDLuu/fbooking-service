package com.filmbooking.booking_service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.filmbooking.booking_service.models.Booking;
import com.filmbooking.booking_service.models.Ticket;
import com.filmbooking.booking_service.repositories.BookingRepository;

@Configuration
class LoadDatabase {

  private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

  @Profile(value="dev")
  @Bean
  CommandLineRunner initDatabase(BookingRepository repository) {

    return args -> {
      log.info("Preloading " +
        repository.save(new Booking(
          UUID.randomUUID().toString().replace("-", ""),
          UUID.randomUUID().toString().replace("-", ""),
          Long.valueOf(1),
          "user1@dummymail.com",
          Long.valueOf(1),
          "USD",
          BigDecimal.valueOf(10),
          new ArrayList<Ticket>()
      )));

      log.info("Preloading " +
        repository.save(new Booking(
          UUID.randomUUID().toString().replace("-", ""),
          UUID.randomUUID().toString().replace("-", ""),
          Long.valueOf(2),
          "user2@dummymail.com",
          Long.valueOf(1),
          "EUR",
          BigDecimal.valueOf(18),
          new ArrayList<Ticket>()
      )));
    };
  }
}
