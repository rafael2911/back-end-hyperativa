package com.hyperativa.api.service;

import com.hyperativa.api.dto.AuthRequestDTO;
import com.hyperativa.api.dto.AuthResponseDTO;
import com.hyperativa.api.entity.UserEntity;
import com.hyperativa.api.exception.UserAlreadyExistsException;
import com.hyperativa.api.repository.UserEntityRepository;
import com.hyperativa.api.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserEntityRepository userEntityRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDTO register(AuthRequestDTO request) {
        log.info("Registering new user: {}", request.getUsername());

        if (userEntityRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userEntityRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already exists");
        }

        UserEntity userEntity = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userEntityRepository.save(userEntity);
        log.info("User registered successfully: {}", request.getUsername());

        String token = tokenProvider.generateTokenFromUsername(request.getUsername());
        return AuthResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTime())
                .username(request.getUsername())
                .build();
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        log.info("User login attempt: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = tokenProvider.generateToken(authentication);
        log.info("User logged in successfully: {}", request.getUsername());

        return AuthResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTime())
                .username(request.getUsername())
                .build();
    }
}

