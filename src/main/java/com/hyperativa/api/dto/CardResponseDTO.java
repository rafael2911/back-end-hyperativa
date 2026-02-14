package com.hyperativa.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("card_identifier")
    private String cardIdentifier;

    @JsonProperty("card_number_last_digits")
    private String cardNumberLastDigits;

    @JsonProperty("lote_number")
    private String loteNumber;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}

