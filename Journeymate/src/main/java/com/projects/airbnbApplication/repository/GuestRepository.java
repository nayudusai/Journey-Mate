package com.projects.airbnbApplication.repository;

import com.projects.airbnbApplication.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
}
