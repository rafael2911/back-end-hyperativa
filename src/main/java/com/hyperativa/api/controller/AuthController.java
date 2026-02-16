package com.hyperativa.api.controller;

import com.hyperativa.api.dto.AuthRequestDTO;
import com.hyperativa.api.dto.AuthResponseDTO;
import com.hyperativa.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody AuthRequestDTO request) {
        log.info("Register endpoint called for user: {}", request.getUsername());
        try {
            AuthResponseDTO response = authService.register(request);
            log.info("User registered successfully: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/token")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        log.info("Login endpoint called for user: {}", request.getUsername());
        try {
            AuthResponseDTO response = authService.login(request);
            log.info("User login successful: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage());
            throw e;
        }
    }
}

