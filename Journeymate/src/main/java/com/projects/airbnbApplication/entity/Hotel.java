package com.projects.airbnbApplication.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="hotel")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    private String city;

    @Column(columnDefinition = "TEXT[]")
    private List<String> photos = new ArrayList<>();

    @Column(columnDefinition = "TEXT[]")
    private List<String> amenities = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Embedded
    @Column(nullable = false, name = "contact_info")
    private HotelContactInfo contactInfo;

    @Column(nullable = false)
    private Boolean active;

    @OneToMany( mappedBy = "hotel", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Room> rooms;

    @ManyToOne(optional = false)
    private User owner;
}
