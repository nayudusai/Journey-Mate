package com.projects.airbnbApplication.repository;

import com.projects.airbnbApplication.dto.HotelMinPriceDto;
import com.projects.airbnbApplication.entity.Hotel;
import com.projects.airbnbApplication.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice, Long> {
    @Query("""
            SELECT HotelMinPriceDto(i.hotel, AVG(i.price))
            FROM HotelMinPrice i 
            WHERE i.hotel.city = :city 
                  AND i.date BETWEEN :startDate AND :endDate
                  AND i.hotel.active = true   
            GROUP BY i.hotel
            """)
    Page<HotelMinPriceDto> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Integer dateCount,
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
