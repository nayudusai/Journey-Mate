package com.projects.airbnbApplication.handlers;


import com.projects.airbnbApplication.entity.User;
import com.projects.airbnbApplication.security.JwtService;
import com.projects.airbnbApplication.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;

//    @Value("${deployEnv}")
//    private String deployEnv;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) oAuth2AuthenticationToken.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        User user = userService.getUserByEmail(email);

        if(user == null) {
             user = User.builder()
                    .name(oAuth2User.getName())
                     .email(email)
                    .build();

             user = userService.save(user);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Cookie cookie =  new Cookie("access_token", accessToken);
        cookie.setHttpOnly(true);
//        cookie.setSecure("production".equals(deployEnv));
        response.addCookie(cookie);

        String frontendUrl = "http://localhost:8080/html?token=" + accessToken;
        response.sendRedirect(frontendUrl);

    }
}
