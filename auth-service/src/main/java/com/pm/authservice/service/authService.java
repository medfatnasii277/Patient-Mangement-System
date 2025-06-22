package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class authService {



    private final UserService userService;
    



    public authService(UserDetailsService userDetailsService, UserService userService) {
        this.userService = userService;

    }

    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
        Optional<String> token = userService.findByEmail(loginRequestDTO.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(),
                        u.getPassword()))
                .map(u -> jwtUtil.generateToken(u.getEmail(), u.getRole()));

        return token;
    }



}
