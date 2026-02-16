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

    @JsonProperty("loteNumber")
    private String loteNumber;

    @JsonProperty("totalCards")
    private Integer totalCards;

    @JsonProperty("processedCards")
    private Integer processedCards;

    private String status;

    @JsonProperty("errorMessage")
    private String errorMessage;
}

