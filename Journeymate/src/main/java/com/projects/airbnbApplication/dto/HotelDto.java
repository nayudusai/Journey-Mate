package com.projects.airbnbApplication.dto;

import com.projects.airbnbApplication.entity.HotelContactInfo;
import com.projects.airbnbApplication.entity.Room;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class HotelDto {

    private Long id;

    private String name;

    private String city;

    private List<String> photos = new ArrayList<>();

    private List<String> amenities = new ArrayList<>();

    private HotelContactInfo contactInfo;

    private Boolean active;
}
