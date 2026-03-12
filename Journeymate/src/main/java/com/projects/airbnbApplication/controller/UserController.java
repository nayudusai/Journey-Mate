package com.projects.airbnbApplication.controller;

import com.projects.airbnbApplication.dto.BookingDto;
import com.projects.airbnbApplication.dto.GuestDto;
import com.projects.airbnbApplication.dto.ProfileUpdateRequestDto;
import com.projects.airbnbApplication.dto.UserDto;
import com.projects.airbnbApplication.service.BookingService;
import com.projects.airbnbApplication.service.GuestService;
import com.projects.airbnbApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {


    private final UserService userService;
    private final BookingService bookingService;
    private final GuestService guestService;

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto) {
        userService.updateProfile(profileUpdateRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myBookings")
    public ResponseEntity<List<BookingDto>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PostMapping("/add-guest")
    public ResponseEntity<GuestDto> addGuest(@RequestParam GuestDto guestDto) {
        guestService.addGuest(guestDto);
        return ResponseEntity.ok(guestDto);
    }

    @PatchMapping("/update-guest")
    public ResponseEntity<Void>  updateGuest(@RequestParam Long guestId, @RequestBody GuestDto guestDto) throws AccessDeniedException {
        guestService.updateGuest(guestId, guestDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-guest")
    public ResponseEntity<Void> deleteGuest(@RequestParam Long guestId) throws AccessDeniedException {
        guestService.deleteGuest(guestId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<List<GuestDto>> getAllGuests() {
        guestService.getAllGuests();
        return ResponseEntity.noContent().build();
    }
}
