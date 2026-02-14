package com.hyperativa.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchUploadResponseDTO {

    @JsonProperty("lote_number")
    private String loteNumber;

    @JsonProperty("total_cards")
    private Integer totalCards;

    @JsonProperty("processed_cards")
    private Integer processedCards;

    private String status;

    @JsonProperty("error_message")
    private String errorMessage;
}

