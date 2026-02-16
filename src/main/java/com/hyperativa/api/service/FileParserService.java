package com.hyperativa.api.service;

import com.hyperativa.api.dto.CardRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FileParserService {

    /**
     * Parses a TXT file content with the format:
     * Header: DESAFIO-HYPERATIVA [01-29]NOME [30-37]DATA [38-45]LOTE [46-51]QTD
     * Data: [01-01]ID_LINHA [02-07]NÚMERO [08-26]NÚMERO_CARTÃO
     * Footer: LOTE [01-08]LOTE [09-14]QTD
     */
    public List<CardRequestDTO> parseCardFile(String fileContent) {
        List<CardRequestDTO> cards = new ArrayList<>();
        String[] lines = fileContent.split("\n");

        if (lines.length < 3) {
            log.warn("File has less than 3 lines");
            return cards;
        }

        String headerLine = lines[0].trim();
        String loteNumber = extractLoteNumber(headerLine);

        // Process data lines (skip header and footer)
        for (int i = 1; i < lines.length - 1; i++) {
            String line = lines[i];
            if (line.isEmpty()) continue;

            try {
                CardRequestDTO card = parseCardLine(line, loteNumber);
                if (card != null && !card.getCardNumber().isEmpty()) {
                    cards.add(card);
                }
            } catch (Exception e) {
                log.error("Error parsing line {}: {}", i, e.getMessage());
            }
        }

        log.info("Parsed {} cards from file with lote number: {}", cards.size(), loteNumber);
        return cards;
    }

    private String extractLoteNumber(String headerLine) {
        // Extract lote number from header (positions 38-45)
        if (headerLine.length() >= 45) {
            return headerLine.substring(37, 45).trim();
        }
        return "UNKNOWN";
    }

    private CardRequestDTO parseCardLine(String line, String defaultLoteNumber) {
        // Ensure line has minimum length and is not a footer
        if (line.startsWith("LOTE")) {
            return null;
        }

        try {
            // [01-07] Card identifier (C1, C2, etc.)
            String cardIdentifier = line.substring(0, Math.min(7, line.length())).trim();
            if (cardIdentifier.isEmpty()) {
                return null;
            }

            // [08-26] Card number (19 positions)
            String cardNumber = "";
            int maxLength = Math.min(line.length(), 26);
            if (maxLength >= 7) {
                cardNumber = line.substring(7, maxLength).trim();
            }

            if (cardNumber.isEmpty() || !cardNumber.matches("\\d+")) {
                return null;
            }

            return CardRequestDTO.builder()
                    .cardNumber(cardNumber)
                    .cardIdentifier(cardIdentifier)
                    .loteNumber(defaultLoteNumber)
                    .build();
        } catch (Exception e) {
            log.error("Error parsing card line: {}", e.getMessage());
            return null;
        }
    }
}

