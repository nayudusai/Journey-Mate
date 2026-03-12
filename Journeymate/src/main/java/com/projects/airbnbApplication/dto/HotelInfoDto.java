package com.projects.airbnbApplication.dto;

import com.projects.airbnbApplication.entity.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelInfoDto {
    private HotelDto hotel;
    private List<RoomPriceResponseDto> room;
}
