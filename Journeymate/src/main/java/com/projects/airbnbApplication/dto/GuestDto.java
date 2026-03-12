package com.projects.airbnbApplication.dto;

import com.projects.airbnbApplication.entity.User;
import com.projects.airbnbApplication.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class GuestDto {

    private Long id;
//  private User user;
    private String name;
    private Gender gender;
    private Integer age;
}
