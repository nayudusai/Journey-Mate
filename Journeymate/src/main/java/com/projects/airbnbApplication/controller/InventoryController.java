package com.projects.airbnbApplication.controller;

import com.projects.airbnbApplication.dto.InventoryDto;
import com.projects.airbnbApplication.dto.UpdateInventoryRequestDto;
import com.projects.airbnbApplication.entity.Inventory;
import com.projects.airbnbApplication.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {


    private final InventoryService inventoryService;


    @GetMapping("/rooms/{roomId}")
     public ResponseEntity<List<InventoryDto>> getInventory(@PathVariable Long roomId) {
        List<InventoryDto> inventoryDto = inventoryService.getInventoryOfRoom(roomId);
        return ResponseEntity.ok(inventoryDto);
    }

    @PatchMapping("rooms/{roomId}")
    public ResponseEntity<Void> updateInventory(@PathVariable Long roomId, @RequestBody UpdateInventoryRequestDto UpdateInventoryDto) {

        inventoryService.updateInventory(roomId, UpdateInventoryDto);
        return  ResponseEntity.noContent().build();
    }
}
