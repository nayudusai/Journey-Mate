package com.projects.airbnbApplication.service;

import com.projects.airbnbApplication.dto.ProfileUpdateRequestDto;
import com.projects.airbnbApplication.dto.SignUpDto;
import com.projects.airbnbApplication.dto.UserDto;
import com.projects.airbnbApplication.entity.User;
import com.projects.airbnbApplication.entity.enums.Role;
import com.projects.airbnbApplication.exception.ResourceNotFoundException;
import com.projects.airbnbApplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new BadCredentialsException("user not found with the id {}" + username));
    }


    public UserDto signUp(SignUpDto signUpDto) {
        Optional<User> user = userRepository.findByEmail(signUpDto.getEmail());

        if (user.isPresent()) {
            throw new BadCredentialsException("user already exists");
        }

        User newUser = modelMapper.map(signUpDto, User.class);
        newUser.setRoles(Set.of(Role.GUEST, Role.ADMIN));
        newUser.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        userRepository.save(newUser);

        return modelMapper.map(newUser, UserDto.class);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).
                orElseThrow(() -> new ResourceNotFoundException("user not found with id " + userId));
    }

    public User  getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User save(User user) {
        return this.userRepository.save(user);
    }

    public void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto) {
        User user = getCurrentUser();

        if(profileUpdateRequestDto.getName() != null) user.setName(profileUpdateRequestDto.getName());
        if(profileUpdateRequestDto.getGender() != null) user.setGender(profileUpdateRequestDto.getGender());
        if(profileUpdateRequestDto.getDateOfBirth() != null) user.setDateOfBirth(profileUpdateRequestDto.getDateOfBirth());
    }

    public UserDto getMyProfile() {
        User user = getCurrentUser();
        return modelMapper.map(user, UserDto.class);
    }

    public User getCurrentUser() {
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}
