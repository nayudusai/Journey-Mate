package com.projects.airbnbApplication.dto;

import com.projects.airbnbApplication.entity.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelMinPriceDto {

    private Hotel hotel;

    private Double price;

}
