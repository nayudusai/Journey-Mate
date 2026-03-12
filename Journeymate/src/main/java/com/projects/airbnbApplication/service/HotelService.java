package com.projects.airbnbApplication.service;

import com.projects.airbnbApplication.dto.HotelDto;
import com.projects.airbnbApplication.dto.HotelInfoDto;
import com.projects.airbnbApplication.dto.HotelInfoRequestDto;
import com.projects.airbnbApplication.dto.HotelReportsDto;
import com.projects.airbnbApplication.entity.Hotel;
import com.projects.airbnbApplication.entity.User;
import org.jspecify.annotations.Nullable;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public interface HotelService {

    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id, HotelDto hotelDto);

    void deleteHotelById(Long id);

    void activateHotel(Long id);

    HotelInfoDto getHotelInfoById(Long hotelId, HotelInfoRequestDto hotelInfoRequestDto) throws AccessDeniedException;

    List<HotelDto> getAllHotelsOfOwner(User user);

    Hotel findById(Long hotelId);

    HotelReportsDto getReportsOfHotel(Long hotelId, LocalDate startDate, LocalDate endDate) throws AccessDeniedException;
}
