package com.projects.airbnbApplication.controller;

import com.projects.airbnbApplication.dto.BookingDto;
import com.projects.airbnbApplication.dto.HotelDto;
import com.projects.airbnbApplication.dto.HotelReportsDto;
import com.projects.airbnbApplication.entity.User;
import com.projects.airbnbApplication.service.BookingService;
import com.projects.airbnbApplication.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelAdminController {

    private final HotelService hotelService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto) {
        log.info("Creating a new Hotel with name : " + hotelDto.getName());
        HotelDto hotel = hotelService.createNewHotel(hotelDto);
        return new  ResponseEntity<>(hotel, HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId) {
        HotelDto hotel = hotelService.getHotelById(hotelId);
        return ResponseEntity.ok(hotel);
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long hotelId, @RequestBody HotelDto hotelDto) {
        hotelService.updateHotelById(hotelId, hotelDto);
        return ResponseEntity.ok(hotelDto);
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId) {
        hotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}")
    public ResponseEntity<Void> activateHotelById(@PathVariable Long hotelId) {
        hotelService.activateHotel(hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels(@PathVariable User user) {
        List<HotelDto> hotels = hotelService.getAllHotelsOfOwner(user);
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/{hotelId}/bookings")
    public ResponseEntity<List<BookingDto>> getBookings(@PathVariable Long hotelId) throws AccessDeniedException {
        List<BookingDto> bookings = bookingService.getAllBookingsByHotelId(hotelId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{hotelId}/reports")
    public ResponseEntity<HotelReportsDto> getHotelReports(@PathVariable Long hotelId,
                                                           @RequestParam(required = false) LocalDate startDate,
                                                           @RequestParam(required = false) LocalDate endDate) throws AccessDeniedException {

        if(startDate == null) startDate = LocalDate.now().minusDays(1);
        if(endDate == null) endDate = LocalDate.now();

        HotelReportsDto reportsDto = hotelService.getReportsOfHotel(hotelId, startDate, endDate);
        return ResponseEntity.ok(reportsDto);
    }

}
