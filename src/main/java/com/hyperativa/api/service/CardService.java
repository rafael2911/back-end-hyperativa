package com.hyperativa.api.service;

import com.hyperativa.api.dto.BatchUploadResponseDTO;
import com.hyperativa.api.dto.CardRequestDTO;
import com.hyperativa.api.dto.CardResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface CardService {

    CardResponseDTO createCard(CardRequestDTO request, String username);

    BatchUploadResponseDTO createCardsFromBatch(MultipartFile file, String username);

    CardResponseDTO getCard(Long cardId, String username);

    CardResponseDTO searchCardByCardNumber(String cardNumber, String username);

}
