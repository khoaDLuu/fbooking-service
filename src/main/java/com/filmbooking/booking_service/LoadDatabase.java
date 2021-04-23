package com.filmbooking.booking_service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LoadDatabase {

  private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

  @Bean
  CommandLineRunner initDatabase(BookingRepository repository) {

    return args -> {
      log.info("Preloading " +
        repository.save(new Booking(
          "NiHBINKn77Fs",
          "NON9niniwn4b",
          Long.valueOf(1),
          "USD",
          BigDecimal.valueOf(10),
          LocalDateTime.now()
      )));

      log.info("Preloading " +
        repository.save(new Booking(
          "IBIbGBIbIBUV8b",
          "jnfsiV7BB2INKn",
          Long.valueOf(2),
          "EUR",
          BigDecimal.valueOf(18),
          LocalDateTime.now()
      )));
    };
  }
}
