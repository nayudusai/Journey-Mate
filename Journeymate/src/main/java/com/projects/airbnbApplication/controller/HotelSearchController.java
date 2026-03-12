package com.projects.airbnbApplication.controller;

import com.projects.airbnbApplication.dto.HotelDto;
import com.projects.airbnbApplication.dto.HotelInfoDto;
import com.projects.airbnbApplication.dto.HotelInfoRequestDto;
import com.projects.airbnbApplication.dto.HotelInventoryRequest;
import com.projects.airbnbApplication.entity.Hotel;
import com.projects.airbnbApplication.service.HotelService;
import com.projects.airbnbApplication.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelSearchController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelDto>> searchHotels(@RequestBody HotelInventoryRequest inventoryRequest) {
         Page<HotelDto> page = inventoryService.searchHotels(inventoryRequest);
         return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfoById(@PathVariable("hotelId") Long hotelId, @RequestBody HotelInfoRequestDto hotelInfoRequestDto) throws AccessDeniedException {
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId, hotelInfoRequestDto));
    }

}
