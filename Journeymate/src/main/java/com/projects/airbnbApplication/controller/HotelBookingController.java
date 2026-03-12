package com.projects.airbnbApplication.controller;

import com.projects.airbnbApplication.dto.*;
import com.projects.airbnbApplication.entity.Hotel;
import com.projects.airbnbApplication.entity.enums.BookingStatus;
import com.projects.airbnbApplication.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.core.SerializableString;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/initialize")
    public ResponseEntity<BookingDto> initializeBooking(
            @RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(bookingService.initializeBooking(bookingRequest));
    }

    @PostMapping("/{bookingId}/add-guests")
    public ResponseEntity<BookingDto> addGuests(@PathVariable Long bookingId, @RequestBody List<GuestDto> guestDto) {
        return ResponseEntity.ok(bookingService.addGuests(bookingId, guestDto));
    }

    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String, String>> initializePayments(@PathVariable Long bookingId) {
        String sessionUrl =  bookingService.initializePayments(bookingId);
        return ResponseEntity.ok(Map.of("sessionUrl", sessionUrl));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookingId}/status")
    public ResponseEntity<BookingStatus> getStatus(@PathVariable Long bookingId) {
        bookingService.getPaymentStatus(bookingId);
        return ResponseEntity.ok(bookingService.getPaymentStatus(bookingId));
    }
}
