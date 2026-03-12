package com.projects.airbnbApplication.service;

import com.projects.airbnbApplication.dto.GuestDto;
import com.projects.airbnbApplication.entity.Guest;
import com.projects.airbnbApplication.entity.User;
import com.projects.airbnbApplication.repository.GuestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static com.projects.airbnbApplication.util.UserUtils.getCurrentUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {
    private final ModelMapper modelMapper;
    private final GuestRepository guestRepository;


    @Override
    public GuestDto addGuest(GuestDto guestDto) {
        User user = getCurrentUser();
        log.info("adding guest to the user {}", user);
        Guest guest = modelMapper.map(guestDto, Guest.class);
        guest.setUser(user);
        Guest savedGuest = guestRepository.save(guest);
        log.info("Guest added to the user with ID {}", savedGuest.getId());
        return modelMapper.map(savedGuest, GuestDto.class);
    }

    @Override
    public void updateGuest(Long guestId, GuestDto guestDto) throws AccessDeniedException {
        log.info("updating guest with ID: {}", guestId);
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new EntityNotFoundException("Guest with ID: " + guestId + " not found"));

        User user = getCurrentUser();
        if(!user.equals(guest.getUser())) {
            throw new AccessDeniedException("You are not authorised to update the guest details");
        }

        modelMapper.map(guestDto, guest);
        guest.setUser(user);
        guest.setId(guestId);
        guestRepository.save(guest);
        log.info("successfully updated the guest with ID {}", guestId);
    }

    @Override
    public void deleteGuest(Long guestId) throws AccessDeniedException {
        log.info("deleting guest with ID: {}", guestId);
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new EntityNotFoundException("Guest with ID: " + guestId + " not found"));

        User user = getCurrentUser();
        if(!user.equals(guest.getUser())) {
            throw new AccessDeniedException("You are not authorised to delete the guest  details");
        }

        guestRepository.delete(guest);
        log.info("successfully deleted the guest with ID {}", guestId);
    }

    @Override
    public List<GuestDto> getAllGuests() {
        User user = getCurrentUser();
        List<Guest> guests = guestRepository.findAll();
        return guests.stream()
                .map(guest -> modelMapper.map(guest, GuestDto.class))
                .toList();
    }
}
