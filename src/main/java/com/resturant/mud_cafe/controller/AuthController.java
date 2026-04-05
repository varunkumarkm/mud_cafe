package com.resturant.mud_cafe.controller;

import com.resturant.mud_cafe.dto.request.LoginRequest;
import com.resturant.mud_cafe.dto.response.AuthResponse;
import com.resturant.mud_cafe.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // TEMPORARY - delete after use
    @GetMapping("/generate-hash")
    public ResponseEntity<String> generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return ResponseEntity.ok(encoder.encode("password123"));
    }
}