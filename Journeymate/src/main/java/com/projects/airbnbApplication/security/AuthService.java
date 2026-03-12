package com.projects.airbnbApplication.security;

import com.projects.airbnbApplication.dto.LoginDto;
import com.projects.airbnbApplication.dto.LoginResponseDto;
import com.projects.airbnbApplication.dto.LoginTokenRes;
import com.projects.airbnbApplication.entity.User;
import com.projects.airbnbApplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public LoginTokenRes login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        String refreshToken = jwtService.generateRefreshToken(user);
        String accessToken = jwtService.generateAccessToken(user);

        return  new LoginTokenRes(refreshToken, accessToken);
    }

    public String refresh(String refreshToken) {
        Long userId = jwtService.findUserIdByToken(refreshToken);
        User user = userRepository.findById(userId).orElse(null);

        return jwtService.generateAccessToken(user);
    }
}
