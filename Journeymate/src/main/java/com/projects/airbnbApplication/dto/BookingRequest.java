package com.projects.airbnbApplication.dto;

import com.projects.airbnbApplication.entity.Guest;
import com.projects.airbnbApplication.entity.Hotel;
import com.projects.airbnbApplication.entity.Room;
import com.projects.airbnbApplication.entity.enums.BookingStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingRequest {
    private Long hotelId;
    private Long roomId;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
