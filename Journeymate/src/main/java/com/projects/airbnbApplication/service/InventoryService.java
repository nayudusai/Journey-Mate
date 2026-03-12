package com.projects.airbnbApplication.service;

import com.projects.airbnbApplication.dto.HotelDto;
import com.projects.airbnbApplication.dto.HotelInventoryRequest;
import com.projects.airbnbApplication.dto.InventoryDto;
import com.projects.airbnbApplication.dto.UpdateInventoryRequestDto;
import com.projects.airbnbApplication.entity.Room;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteFutureInventories(Room room);

    Page<HotelDto> searchHotels(HotelInventoryRequest inventoryRequest);

    List<InventoryDto> getInventoryOfRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryDto);
}