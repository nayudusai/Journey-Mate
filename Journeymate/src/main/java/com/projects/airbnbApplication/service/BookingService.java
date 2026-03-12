package com.projects.airbnbApplication.service;

import com.projects.airbnbApplication.dto.BookingDto;
import com.projects.airbnbApplication.dto.BookingRequest;
import com.projects.airbnbApplication.dto.GuestDto;
import com.projects.airbnbApplication.entity.enums.BookingStatus;
import com.stripe.model.Event;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

public interface BookingService {

    BookingDto initializeBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initializePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    BookingStatus getPaymentStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId) throws AccessDeniedException;

    List<BookingDto> getMyBookings();


}
