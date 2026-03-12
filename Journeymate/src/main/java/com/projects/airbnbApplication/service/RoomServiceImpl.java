package com.projects.airbnbApplication.service;

import com.projects.airbnbApplication.dto.RoomDto;
import com.projects.airbnbApplication.entity.Hotel;
import com.projects.airbnbApplication.entity.Room;
import com.projects.airbnbApplication.entity.User;
import com.projects.airbnbApplication.exception.ResourceNotFoundException;
import com.projects.airbnbApplication.exception.UnAuthorisedException;
import com.projects.airbnbApplication.repository.HotelRepository;
import com.projects.airbnbApplication.repository.RoomRepository;
import com.stripe.model.TODO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import java.util.stream.Collectors;

import static com.projects.airbnbApplication.util.UserUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final ModelMapper modelMapper;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryService inventoryService;

    @Override
    public RoomDto createRoom(Long hotelId, RoomDto roomDto) {

        log.info("Creating room with room in hotel with ID: " + hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> (new ResourceNotFoundException("No hotel with the ID: " + hotelId)));
        Room room = modelMapper.map(roomDto , Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        // TODO : create inventory as room is created and if hotel is active

        if(hotel.getActive()) {
            inventoryService.initializeRoomForAYear(room);
        }
        return modelMapper.map(room , RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms in hotel with ID: " + hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> (new ResourceNotFoundException("No hotel with the ID: " + hotelId)));
        return hotel.getRooms()
                .stream()
                .map(room -> modelMapper.map(room , RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting room with ID: " + roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> (new ResourceNotFoundException("No room with the ID: " + roomId)));
        return  modelMapper.map(room , RoomDto.class);
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        log.info("Deleting room with ID: " + roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> (new ResourceNotFoundException("No room with the ID: " + roomId)));

        // TODO : Delete all feature inventory for this room.
        inventoryService.deleteFutureInventories(room);
        roomRepository.deleteById(roomId);
    }

    @Override
    @Transactional
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("No hotel with the ID: " + hotelId));

        User user = getCurrentUser();

        if(!user.equals(hotel.getOwner())) {
            throw new UnAuthorisedException("Only owner can update the room");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("No room with the ID: " + roomId));

        modelMapper.map(roomDto , Room.class);
        room.setId(roomId);
        roomRepository.save(room);

        // TODO : If price or inventory is updated then update the inventory for this room.

        return modelMapper.map(room , RoomDto.class);
    }
}
