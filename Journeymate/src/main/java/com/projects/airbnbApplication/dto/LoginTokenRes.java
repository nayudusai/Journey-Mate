package com.projects.airbnbApplication.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginTokenRes {
    private String accessToken;
    private String refreshToken;
}
