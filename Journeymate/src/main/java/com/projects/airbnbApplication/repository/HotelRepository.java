package com.projects.airbnbApplication.repository;

import com.projects.airbnbApplication.entity.Hotel;
import com.projects.airbnbApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findHotelByOwner(User owner);
}
