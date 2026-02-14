package com.hyperativa.api.service;

import com.hyperativa.api.dto.AuthRequestDTO;
import com.hyperativa.api.exception.UserAlreadyExistsException;
import com.hyperativa.api.repository.UserEntityRepository;
import com.hyperativa.api.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void testRegisterSuccess() {
        AuthRequestDTO request = AuthRequestDTO.builder()
                .username("newuser")
                .password("password123")
                .email("email@example.com")
                .build();

        when(userEntityRepository.existsByUsername("newuser")).thenReturn(false);
        when(userEntityRepository.existsByEmail("email@example.com")).thenReturn(false);
        when(tokenProvider.generateTokenFromUsername("newuser")).thenReturn("token-value");

        var response = authService.register(request);

        assertNotNull(response);
        assertEquals("token-value", response.getAccessToken());
        assertEquals("newuser", response.getUsername());
        verify(userEntityRepository, times(1)).save(any());
    }

    @Test
    void testRegisterUserNameExists() {
        AuthRequestDTO request = AuthRequestDTO.builder()
                .username("existinguser")
                .password("password123")
                .email("email2@example.com")
                .build();

        when(userEntityRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userEntityRepository, never()).save(any());
    }

    @Test
    void testRegisterEmailExists() {
        AuthRequestDTO request = AuthRequestDTO.builder()
                .username("existinguser")
                .password("password123")
                .email("email2@example.com")
                .build();

        when(userEntityRepository.existsByUsername(any())).thenReturn(false);
        when(userEntityRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userEntityRepository, never()).save(any());
    }

    @Test
    void testLoginSuccess() {
        AuthRequestDTO request = AuthRequestDTO.builder()
                .username("loginuser")
                .password("password123")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken("loginuser", null);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("jwt-token");

        var response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("loginuser", response.getUsername());
    }
}

