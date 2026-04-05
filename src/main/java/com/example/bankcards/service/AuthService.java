package com.example.bankcards.service;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.UserSecurity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(PasswordEncoder passwordEncoder, JwtService jwtService, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public LoginResponse authenticate(LoginRequest loginRequest) {
        User user = userService.findByUsername(loginRequest.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new LoginResponse(jwtService.generateToken(new UserSecurity(user)));
    }
}
