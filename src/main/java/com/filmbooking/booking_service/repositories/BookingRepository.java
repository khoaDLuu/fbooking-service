package com.filmbooking.booking_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.filmbooking.booking_service.models.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Booking findByOrderId(String orderId);
    List<Booking> findByUser(Long userId);
    Booking findByCode(String code);
}
