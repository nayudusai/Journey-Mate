package com.projects.airbnbApplication.repository;

import com.projects.airbnbApplication.dto.RoomPriceDto;
import com.projects.airbnbApplication.entity.Hotel;
import com.projects.airbnbApplication.entity.Inventory;
import com.projects.airbnbApplication.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    void deleteByRoom(Room room);

    @Query("""
            SELECT DISTINCT i.hotel
            FROM Inventory i
            WHERE i.city = :city
                  AND i.date BETWEEN :startDate AND :endDate
                  AND i.closed = false
                  AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount  
            GROUP BY i.hotel,i.room 
            HAVING COUNT(i) = :dateCount 
            """)

    Page<Hotel> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );

    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
                    AND i.date BETWEEN :startDate AND :endDate
                    AND i.closed = false
                    AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
            """)

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockAvailableInventory(
            @Param("roomId")  Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );

    List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT i
            FROM Inventory i 
            WHERE i.room.id = :roomId
                    AND i.date BETWEEN :startDate AND :endDate
                    AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
                    AND i.closed = false
            """)

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockReservedInventory(
            @Param("roomId")  Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.reservedCount = i.reservedCount - :roomsCount,
                i.bookedCount = i.bookedCount + :roomsCount
            WHERE i.room.id = :roomId
                    AND i.date BETWEEN :startDate AND :endDate
                    AND (i.totalCount - i.bookedCount ) >= :roomsCount
                    AND i.reservedCount >= :roomsCount
                    AND i.closed = false
            """)
    void confirmBooking(
            @Param("roomId")  Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.bookedCount = i.bookedCount - :roomsCount
            WHERE i.room.id = :roomId
                        AND i.date BETWEEN :startDate AND :endDate
                        AND (i.totalCount - i.bookedCount ) >= :roomsCount
                        AND i.closed = false
            """)
    void cancelBooking(
            @Param("roomId")  Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );

    @Query("""
            UPDATE Inventory i
            SET i.reservedCount = i.reservedCount + :roomsCount
            WHERE i.room.id = :roomId
                        AND i.date BETWEEN :startDate AND :endDate
                        AND (i.totalCount - i.bookedCount) >= :roomsCount
                        AND i.closed = false
            """)
    List<Inventory> findAndReserveInventory(
            @Param("roomId")  Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );


    List<Inventory> findByRoomOrderByDate(Room room);


    @Query("""
            SELECT i
            FROM Inventory i 
            WHERE i.room.id = :roomId
                    AND i.date BETWEEN :startDate AND :endDate
            """)

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> getLockAndUpdateInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.surgeFactor = :surgeFactor,
                 i.closed = :closed
            WHERE i.date BETWEEN :startDate AND :endDate
            """)

    void updateInventory(@Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate,
                         @Param("roomsCount") BigDecimal surgeFactor,
                         @Param("closed") Boolean closed);

    @Query(
            """
            SELECT new com.projects.airbnbApplication.dto.RoomPriceDto(
                 i.room,
                 AVG (i.price)
            )
            FROM Inventory i 
            WHERE i.hotel.id = :hotelId
                        AND i.date BETWEEN :startDate AND :endDate
                        AND (i.totalCount - i.bookedCount) >= :roomsCount
                        AND i.closed = false
            GROUP BY i.room  
            HAVING COUNT(i) = :daysCount                     
            """)
    List<RoomPriceDto> findRoomAveragePrice(
            @Param("hotelId") Long hotelId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Long roomsCount,
            @Param("daysCount") Long daysCount);

    @Modifying
    @Query(
        """
        UPDATE Inventory i
        SET i.reservedCount = i.reservedCount + :roomsCount
        WHERE i.room.id = :roomId
                AND i.date BETWEEN :checkInDate AND :checkOutDate
                AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
                AND i.closed = false
        """)
    void initializeBooking(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("roomsCount") Integer roomsCount);
}
