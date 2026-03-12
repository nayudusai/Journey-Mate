package com.projects.airbnbApplication.service;

import com.projects.airbnbApplication.dto.GuestDto;
import com.projects.airbnbApplication.entity.Guest;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface GuestService {

    GuestDto addGuest(GuestDto guestDto);

    void updateGuest(Long guestId, GuestDto guestDto) throws AccessDeniedException;

    void deleteGuest(Long guestId) throws AccessDeniedException;

    List<GuestDto> getAllGuests();
}
