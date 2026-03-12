package com.projects.airbnbApplication.security;

import com.projects.airbnbApplication.entity.User;
import com.projects.airbnbApplication.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            log.info("incoming request: {}", request.getRequestURI());
            final String authorizationHeader = request.getHeader("Authorization");

            if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String authToken = authorizationHeader.split("Bearer ")[1];
            Long userId = jwtService.findUserIdByToken(authToken);

//            System.out.println("Authorization Header: " + authorizationHeader);
//            System.out.println("Extracted token : " + authToken);
//            System.out.println("user ID : " + userId);

            log.info("verifying securityContextHolder() object");
            System.out.println("Before setting auth: " +
                    SecurityContextHolder.getContext().getAuthentication());

//            Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
            if(userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.getUserById(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("After setting auth: " +
                        SecurityContextHolder.getContext().getAuthentication());
            }
            filterChain.doFilter(request, response);
        }catch (JwtException ex) {
            handlerExceptionResolver.resolveException(request,response,null, ex);
        }
    }
}
