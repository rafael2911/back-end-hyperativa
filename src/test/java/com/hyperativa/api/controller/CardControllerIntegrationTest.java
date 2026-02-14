package com.hyperativa.api.controller;

import com.hyperativa.api.dto.CardRequestDTO;
import com.hyperativa.api.entity.CardEntity;
import com.hyperativa.api.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardControllerIntegrationTest extends AbstractIT {

    @Test
    void testCreateCard_success() throws Exception {
        CardRequestDTO request = CardRequestDTO.builder()
                .cardIdentifier("CARD001")
                .cardNumber("4456897999999999")
                .build();

        mockMvc.perform(post("/v1/cards")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cardIdentifier").value("CARD001"));

        assertTrue(cardEntityRepository.existsByCardIdentifier("CARD001"));
    }

    @Test
    void testCreateCardWithoutAuth_fails() throws Exception {
        CardRequestDTO request = CardRequestDTO.builder()
                .cardIdentifier("CARD002")
                .cardNumber("4456897999999999")
                .build();

        mockMvc.perform(post("/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetCardById_success() throws Exception {
        // Create a card for the test user
        CardEntity card = CardEntity.builder()
                .cardIdentifier("GET_CARD_001")
                .user(testUser)
                .cardNumberEncrypted("bCQlT1+F8tmigBjZiaxQjYY+Qy5/10QUBywuczmibRA=")
                .cardNumberLastDigits("9999")
                .build();
        CardEntity savedCard = cardEntityRepository.save(card);

        mockMvc.perform(get("/v1/cards/{id}", savedCard.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCard.getId()))
                .andExpect(jsonPath("$.cardIdentifier").value("GET_CARD_001"));
    }

    @Test
    void testGetCardUnauthorizedUser_fails() throws Exception {
        // Create card for different user
        UserEntity otherUser = userEntityRepository.save(UserEntity.builder()
                .username("otheruser")
                .email("other@example.com")
                .password(passwordEncoder.encode("password123"))
                .build());

        CardEntity card = CardEntity.builder()
                .cardIdentifier("OTHER_CARD")
                .cardNumberEncrypted("j085uMvUdmmqdPRmEMZC84Y+Qy5/10QUBywuczmibRA=")
                .cardNumberLastDigits("9999")
                .user(otherUser)
                .build();
        CardEntity savedCard = cardEntityRepository.save(card);

        mockMvc.perform(get("/v1/cards/{id}", savedCard.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetCardNotFound_fails() throws Exception {
        mockMvc.perform(get("/v1/cards/{id}", 99999L)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCardDuplicateIdentifier_fails() throws Exception {
        CardRequestDTO request1 = CardRequestDTO.builder()
                .cardIdentifier("DUP_CARD")
                .cardNumber("4456897999999999")
                .build();

        // First creation should succeed
        mockMvc.perform(post("/v1/cards")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Second creation with same identifier should fail
        mockMvc.perform(post("/v1/cards")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isConflict());
    }
}
