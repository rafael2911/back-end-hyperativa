package com.hyperativa.api.controller;

import com.hyperativa.api.dto.CardRequestDTO;
import com.hyperativa.api.entity.CardEntity;
import com.hyperativa.api.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardControllerIntegrationTest extends AbstractIT {

    @Test
    void testCreateCardShouldReturnSuccess() throws Exception {
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
    void testCreateCardWithoutAuthShouldReturnForbidden() throws Exception {
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
    void testGetCardByIdShouldReturnSuccess() throws Exception {
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
    void testGetCardUnauthorizedUserShouldReturnForbidden() throws Exception {
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
    void testGetCardNotFoundShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/v1/cards/{id}", 99999L)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCardDuplicateIdentifierShouldReturnConflict() throws Exception {
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


    @Test
    void testUploadCardBatchWhenDuplicateLoteShouldReturnFailed() throws Exception {
        String batchContent = """
                DESAFIO-HYPERATIVA                 2026-02-16 BATCH03  1
                1       4456897000000004
                LOTE        BATCH03      1
                """;

        // First upload should succeed
        mockMvc.perform(multipart("/v1/cards/batch")
                .file(new MockMultipartFile("file", "cards1.txt", "text/plain", batchContent.getBytes()))
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Second upload with same lote should fail
        mockMvc.perform(multipart("/v1/cards/batch")
                .file(new MockMultipartFile("file", "cards2.txt", "text/plain", batchContent.getBytes()))
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void testUploadCardBatchWhenMultipleCardsShouldReturnCompleted() throws Exception {
        String batchContent = """
        DESAFIO-HYPERATIVA                 2026-02-16 BATCH04  3
        CARD1   4456897000111111
        CARD2   4456897000222222
        CARD3   4456897000333333
        LOTE        BATCH04      3
        """;

        mockMvc.perform(multipart("/v1/cards/batch")
                .file(new MockMultipartFile("file", "batch_cards.txt", "text/plain", batchContent.getBytes()))
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalCards").value(3))
                .andExpect(jsonPath("$.processedCards").value(3));
    }

    @Test
    void testSearchCardByNumberShouldReturnSuccess() throws Exception {
        // create a card via API so it's stored with encryption/owner
        CardRequestDTO request = CardRequestDTO.builder()
                .cardIdentifier("SEARCH_CARD_001")
                .cardNumber("4456897999999911")
                .build();

        mockMvc.perform(post("/v1/cards")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cardIdentifier").value("SEARCH_CARD_001"));

        // now search by the full card number using header 'cardNumber'
        mockMvc.perform(get("/v1/cards/search")
                .header("Authorization", "Bearer " + authToken)
                .header("cardNumber", "4456897999999911")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardIdentifier").value("SEARCH_CARD_001"));
    }

    @Test
    void testSearchCardByNumberShouldReturnNotFound() throws Exception {
        // search for a number that doesn't exist
        mockMvc.perform(get("/v1/cards/search")
                .header("Authorization", "Bearer " + authToken)
                .header("cardNumber", "0000000000000000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
