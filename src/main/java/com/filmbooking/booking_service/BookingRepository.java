package com.filmbooking.booking_service;

import org.springframework.data.jpa.repository.JpaRepository;

import com.filmbooking.booking_service.models.Booking;

interface BookingRepository extends JpaRepository<Booking, Long> {
    Booking findByOrderId(String orderId);
}
