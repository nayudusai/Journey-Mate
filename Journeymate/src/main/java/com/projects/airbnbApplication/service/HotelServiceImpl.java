package com.projects.airbnbApplication.service;

import com.projects.airbnbApplication.dto.*;
import com.projects.airbnbApplication.entity.Booking;
import com.projects.airbnbApplication.entity.Hotel;
import com.projects.airbnbApplication.entity.Room;
import com.projects.airbnbApplication.entity.User;
import com.projects.airbnbApplication.entity.enums.BookingStatus;
import com.projects.airbnbApplication.exception.ResourceNotFoundException;
import com.projects.airbnbApplication.exception.UnAuthorisedException;
import com.projects.airbnbApplication.repository.BookingRepository;
import com.projects.airbnbApplication.repository.HotelRepository;
import com.projects.airbnbApplication.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.projects.airbnbApplication.util.UserUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomService roomService;
    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;
    private final UserService userService;


    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating hotel with name " + hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto , Hotel.class);
        hotel.setActive(false);
        User user = getCurrentUser();
        hotel.setOwner(user);
        hotel = hotelRepository.save(hotel);
        log.info("created a new hotel with ID: " + hotel.getId());
        return modelMapper.map(hotel , HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
         log.info("Getting hotel with id " + id);
         Hotel hotel = hotelRepository
                 .findById(id)
                 .orElseThrow(()->(new ResourceNotFoundException("Hotel not found with id: " + id)));

         User user = getCurrentUser();
         if (!user.equals(hotel.getOwner())) {
             throw new UnAuthorisedException("You are not authorized to view the hotel details");
         }
         return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating hotel with id " + id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id: " + id));

        User user =  (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!hotel.getOwner().equals(user)) {
            throw new UnAuthorisedException("You are not authorized to update the hotel details");
        }

        modelMapper.map(hotelDto , hotel);
        hotel.setId(id);
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel , HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long hotelId) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        User user =  (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!hotel.getOwner().equals(user)) {
            throw new UnAuthorisedException("You are not authorized to delete the hotel : {}" + hotelId );
        }

        for(Room room : hotel.getRooms()) {
            inventoryService.deleteFutureInventories(room);
            roomService.deleteRoomById(room.getId());
        }
        hotelRepository.deleteById(hotelId);
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating the hotel with id {} " , hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        User user =  (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!hotel.getOwner().equals(user)) {
            throw new UnAuthorisedException("You are not authorized to activate the hotel details");
        }

        hotel.setActive(true);

        for(Room room : hotel.getRooms()) {
            inventoryService.initializeRoomForAYear(room);
        }
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId, HotelInfoRequestDto hotelInfoRequestDto) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        long daysCount = ChronoUnit.DAYS.between(hotelInfoRequestDto.getStartDate(), hotelInfoRequestDto.getEndDate());

        List<RoomPriceDto> roomPriceDtoList = inventoryRepository.findRoomAveragePrice(hotelId,
                hotelInfoRequestDto.getStartDate(),
                hotelInfoRequestDto.getEndDate(),
                hotelInfoRequestDto.getRoomsCount(), daysCount);

        List<RoomPriceResponseDto> rooms =roomPriceDtoList.stream()
                .map(roomPriceDto -> {
                    RoomPriceResponseDto roomPriceResponseDto = modelMapper.map(roomPriceDto, RoomPriceResponseDto.class);
                    roomPriceResponseDto.setPrice(roomPriceDto.getPrice());
                    return roomPriceResponseDto;
                })
                .collect(Collectors.toList());

        return new HotelInfoDto(modelMapper.map(hotel , HotelDto.class), rooms);
    }

    @Override
    public List<HotelDto> getAllHotelsOfOwner(User user) {
        User user1 = getCurrentUser();

        if(!user1.equals(user)) {
            throw new UnAuthorisedException("You are not authorized to access hotels details");
        }

        List<Hotel> hotels = hotelRepository.findHotelByOwner(user1);
        return hotels.stream()
                .map((element) -> modelMapper.map(element, HotelDto.class))
                .toList();
    }

    @Override
    public Hotel findById(Long hotelId) {
        return  hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> (new ResourceNotFoundException("No hotel with the ID: " + hotelId)));
    }

    @Override
    @Transactional
    public HotelReportsDto getReportsOfHotel(Long hotelId, LocalDate startDate, LocalDate endDate) throws AccessDeniedException {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> (new ResourceNotFoundException("No hotel with the ID: " + hotelId)));

        User user = getCurrentUser();

        log.info("verifying wether the current user is the owner of the hotel");

        if(!user.equals(hotel.getOwner())) {
            throw new AccessDeniedException("booking user and current user should be same");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);

        Long totalConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenue = bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .map(booking -> booking.getPrice())
                .reduce(BigDecimal.ZERO , BigDecimal::add);

        BigDecimal averageRevenue = totalConfirmedBookings == 0 ? BigDecimal.ZERO:
                totalRevenue.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportsDto(totalConfirmedBookings, totalRevenue, averageRevenue );
    }
}