package com.hyperativa.api.controller;

import com.hyperativa.api.dto.AuthRequestDTO;
import com.hyperativa.api.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class AuthControllerIntegrationTest extends AbstractIT {

    @Test
    void testRegisterNewUser_success() throws Exception {
        AuthRequestDTO request = AuthRequestDTO.builder()
                .username("newuser")
                .password("password123")
                .email("newuser@example.com")
                .build();

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.token_type").value("Bearer"));

        assertTrue(userEntityRepository.existsByUsername("newuser"));
        assertTrue(userEntityRepository.existsByEmail("newuser@example.com"));
    }

    @Test
    void testRegisterDuplicateUsername_fails() throws Exception {
        // Create existing user
        userEntityRepository.save(UserEntity.builder()
                .username("existinguser")
                .email("existing@example.com")
                .password(passwordEncoder.encode("password123"))
                .build());

        AuthRequestDTO request = AuthRequestDTO.builder()
                .username("existinguser")
                .password("password456")
                .email("another@example.com")
                .build();

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void testLoginSuccess() throws Exception {
        String username = "loginuser";
        String password = "password123";

        userEntityRepository.save(UserEntity.builder()
                .username(username)
                .email("login@example.com")
                .password(passwordEncoder.encode(password))
                .build());

        AuthRequestDTO request = AuthRequestDTO.builder()
                .username(username)
                .password(password)
                .build();

        mockMvc.perform(post("/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.token_type").value("Bearer"));
    }

    @Test
    void testLoginWithInvalidPassword_fails() throws Exception {
        String username = "testuser";
        userEntityRepository.save(UserEntity.builder()
                .username(username)
                .email("test@example.com")
                .password(passwordEncoder.encode("correctpassword"))
                .build());

        AuthRequestDTO request = AuthRequestDTO.builder()
                .username(username)
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

}
