package com.projects.airbnbApplication.repository;

import com.projects.airbnbApplication.dto.BookingDto;
import com.projects.airbnbApplication.entity.Booking;
import com.projects.airbnbApplication.entity.Hotel;
import com.projects.airbnbApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.ScopedValue;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByPaymentSessionId(String paymentSessionId);

    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDate, LocalDateTime endDate);

    List<Booking> getAllBookingsByHotelId(Hotel hotel);

    List<Booking> findByUser(User user);
}
