package com.projects.airbnbApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelReportsDto {

    private Long totalBookings;
    private BigDecimal totalRevenue;
    private BigDecimal averageRevenue;
}
