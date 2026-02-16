package com.hyperativa.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRequestDTO {

    @NotBlank(message = "Card number is required")
    @Size(min = 13, max = 19, message = "Card number must be between 13 and 19 digits")
    private String cardNumber;

    @NotBlank(message = "Card identifier is required")
    private String cardIdentifier;

    private String loteNumber;
}

