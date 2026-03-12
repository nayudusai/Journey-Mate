package com.projects.airbnbApplication.dto;

import com.projects.airbnbApplication.entity.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Gender gender;
    private LocalDate dateOfBirth;
}
