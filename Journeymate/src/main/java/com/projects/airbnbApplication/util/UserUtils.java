package com.projects.airbnbApplication.util;

import com.projects.airbnbApplication.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class UserUtils {

    public static User getCurrentUser() {
       return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}
