package com.hyperativa.api.controller;

import com.hyperativa.api.dto.BatchUploadResponseDTO;
import com.hyperativa.api.dto.CardRequestDTO;
import com.hyperativa.api.dto.CardResponseDTO;
import com.hyperativa.api.service.CardService;
import com.hyperativa.api.util.CardNumberUtils;
import com.hyperativa.api.service.FileParserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/v1/cards")
@AllArgsConstructor
public class CardController {

    private final CardService cardService;
    private final FileParserService fileParserService;

    @PostMapping
    public ResponseEntity<CardResponseDTO> createCard(
            @Valid @RequestBody CardRequestDTO request,
            UriComponentsBuilder uriBuilder,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String username = authentication.getName();
        log.info("Create card endpoint called by user: {} - IP: {}", username, getClientIp(httpRequest));

        CardResponseDTO response = cardService.createCard(request, username);
        URI uri = uriBuilder.path("/v1/cards/{id}")
                .buildAndExpand(response.getId()).toUri();

        log.info("Card created successfully by user: {} with identifier: {}", username, request.getCardIdentifier());
        return ResponseEntity.created(uri).body(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchUploadResponseDTO> uploadCardBatch(
            @RequestHeader MultipartFile file,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String username = authentication.getName();
        log.info("Batch upload endpoint called by user: {} - File: {} - IP: {}",
                username, file.getOriginalFilename(), getClientIp(httpRequest));

        BatchUploadResponseDTO response = cardService.createCardsFromBatch(file, username);

        log.info("Batch uploaded successfully by user: {} - response: {}", username, response);

        return ResponseEntity.ok().body(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDTO> getCardById(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String username = authentication.getName();
        log.info("Get card by ID endpoint called by user: {} for card ID: {} - IP: {}",
                username, id, getClientIp(httpRequest));

        CardResponseDTO response = cardService.getCard(id, username);

        log.info("Card retrieved successfully for user: {} with card ID: {} response: {}", username, id, response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<CardResponseDTO> searchCard(
            @RequestHeader String cardNumber,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String username = authentication.getName();
        String lastDigits = CardNumberUtils.getLastDigits(cardNumber);

        log.info("Search card endpoint called by user: {} with last digits: {} - IP: {}",
                username, lastDigits, getClientIp(httpRequest));

        CardResponseDTO response = cardService.searchCardByCardNumber(cardNumber, username);

        log.info("Card found for user: {} with last digits: {} response: {}", username, lastDigits, response);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor == null || xForwardedFor.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xForwardedFor.split(",")[0];
    }
}

