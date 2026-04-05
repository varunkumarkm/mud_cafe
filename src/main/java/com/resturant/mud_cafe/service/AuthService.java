package com.resturant.mud_cafe.service;

import com.resturant.mud_cafe.dto.request.LoginRequest;
import com.resturant.mud_cafe.dto.response.AuthResponse;
import com.resturant.mud_cafe.exception.ResourceNotFoundException;
import com.resturant.mud_cafe.exception.UnauthorizedException;
import com.resturant.mud_cafe.repository.UserRepository;
import com.resturant.mud_cafe.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive())
            throw new UnauthorizedException("Account is deactivated");

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            throw new UnauthorizedException("Invalid email or password");

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}