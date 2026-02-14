package com.hyperativa.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardResponseDTO {

    private Long id;

    private String cardIdentifier;

    private String cardNumberLastDigits;

    private String loteNumber;

    private LocalDateTime createdAt;

}

