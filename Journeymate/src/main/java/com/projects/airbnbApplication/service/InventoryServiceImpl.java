package com.projects.airbnbApplication.service;

import com.projects.airbnbApplication.dto.HotelDto;
import com.projects.airbnbApplication.dto.HotelInventoryRequest;
import com.projects.airbnbApplication.dto.InventoryDto;
import com.projects.airbnbApplication.dto.UpdateInventoryRequestDto;
import com.projects.airbnbApplication.entity.Hotel;
import com.projects.airbnbApplication.entity.Inventory;
import com.projects.airbnbApplication.entity.Room;
import com.projects.airbnbApplication.entity.User;
import com.projects.airbnbApplication.exception.ResourceNotFoundException;
import com.projects.airbnbApplication.exception.UnAuthorisedException;
import com.projects.airbnbApplication.repository.InventoryRepository;
import com.projects.airbnbApplication.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.projects.airbnbApplication.util.UserUtils.getCurrentUser;

@RequiredArgsConstructor
@Service
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;

    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today  = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for(; !today.isAfter(endDate); today = today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteFutureInventories(Room room) {
        LocalDate today  = LocalDate.now();
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelDto> searchHotels(HotelInventoryRequest inventoryRequest) {
        Pageable pageable = PageRequest.of(inventoryRequest.getPage(),  inventoryRequest.getSize());

        long dateCount = ChronoUnit.DAYS.between(inventoryRequest.getStartDate(), inventoryRequest.getEndDate()) + 1;

        Page<Hotel> hotel = inventoryRepository.findHotelsWithAvailableInventory(inventoryRequest.getCity(),
                inventoryRequest.getStartDate(),
                inventoryRequest.getEndDate(),
                inventoryRequest.getRoomsCount(),
                dateCount,
                pageable
                );
        return hotel.map((element) -> modelMapper.map(element, HotelDto.class));
    }

    @Override
    public List<InventoryDto> getInventoryOfRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        User user = getCurrentUser();

        if(!user.equals(room.getHotel().getOwner())) {
            throw new UnAuthorisedException("only owners can access this information");
        }

        log.info("Getting all inventories of room by Room ID: {} ",roomId);

        return inventoryRepository.findByRoomOrderByDate(room).stream()
                .map((inventory) -> modelMapper.map(inventory, InventoryDto.class))
                .toList();

    }

    @Override
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryDto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        User user = getCurrentUser();

        if(!user.equals(room.getHotel().getOwner())) {
            throw new UnAuthorisedException("only owners can update the inventory");
        }

        inventoryRepository.getLockAndUpdateInventory(roomId,
                        updateInventoryDto.getStartDate(),
                        updateInventoryDto.getEndDate());

        inventoryRepository.updateInventory(updateInventoryDto.getStartDate(),
                updateInventoryDto.getEndDate(),
                updateInventoryDto.getSurgeFactor(),
                updateInventoryDto.getClosed());
    }
}
