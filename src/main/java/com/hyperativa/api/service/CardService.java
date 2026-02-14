package com.hyperativa.api.service;

import com.hyperativa.api.dto.BatchUploadRequestDTO;
import com.hyperativa.api.dto.BatchUploadResponseDTO;
import com.hyperativa.api.dto.CardRequestDTO;
import com.hyperativa.api.dto.CardResponseDTO;
import com.hyperativa.api.entity.CardBatchEntity;
import com.hyperativa.api.entity.CardEntity;
import com.hyperativa.api.entity.UserEntity;
import com.hyperativa.api.exception.*;
import com.hyperativa.api.mapper.CardEntityMapper;
import com.hyperativa.api.repository.CardBatchEntityRepository;
import com.hyperativa.api.repository.CardEntityRepository;
import com.hyperativa.api.repository.UserEntityRepository;
import com.hyperativa.api.security.CryptoService;
import com.hyperativa.api.util.FileParserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CardService {

    public static final String CARD_BY_ID_NOT_FOUND_ERROR_MESSAGE = "Card not found with id: %d";
    public static final String CARD_BY_ID_ENCRYPT_FOUND_ERROR_MESSAGE = "Card not found with encrypt: %s";
    public static final String USER_NOT_FOUND_WITH_USERNAME_ERROR_MESSAGE = "User not found with username: %s";
    public static final String USER_NOT_AUTHORIZED_TO_ACCESS_THIS_CARD_ERROR_MESSAGE = "User, %s, not authorized to access this card, %s";
    public static final String CARD_IDENTIFIER_ALREADY_EXISTS_WITH_IDENTIFIER_ERROR_MESSAGE = "Card identifier already exists with identifier or card number";

    private final CardEntityRepository cardEntityRepository;
    private final CardBatchEntityRepository cardBatchEntityRepository;
    private final UserEntityRepository userEntityRepository;
    private final CryptoService cryptoService;
    private final CardEntityMapper cardEntityMapper;
    private final FileParserService fileParserService;

    public CardResponseDTO createCard(CardRequestDTO request, String username) {
        log.info("Creating new card for user: {} with identifier: {}", username, request.getCardIdentifier());

        UserEntity userEntity = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_WITH_USERNAME_ERROR_MESSAGE.formatted(username)));

        CardEntity cardEntity = cardEntityMapper.fromCardRequestDTO(request);
        cardEntity.setUser(userEntity);

        if (cardValidate(cardEntity)) {
            log.error("CardNumber with  identifier: {} and encryptedCardNumber: {} already exists", cardEntity.getCardIdentifier(), cardEntity.getCardNumberEncrypted());
            throw new CardAlreadyExistsException(CARD_IDENTIFIER_ALREADY_EXISTS_WITH_IDENTIFIER_ERROR_MESSAGE.formatted(request.getCardIdentifier()));
        }

        CardEntity savedCardEntity = cardEntityRepository.save(cardEntity);
        log.info("Card created successfully with id: {} for user: {}", savedCardEntity.getId(), username);

        return cardEntityMapper.toCardResponseDTO(savedCardEntity);
    }

    @Transactional
    public BatchUploadResponseDTO createCardsFromBatch(BatchUploadRequestDTO request, String username) {
        log.info("Processing batch of request {} cards for user: {}", request, username);
        try {
            String fileContent = new String(request.getFile().getBytes());
            List<CardRequestDTO> cards = fileParserService.parseCardFile(fileContent);

            if (cards.isEmpty()) {
                log.warn("No valid cards found in uploaded file by user: {}", username);
                return BatchUploadResponseDTO.builder()
                        .errorMessage("No valid cards found in file")
                        .status("FAILED")
                        .build();
            }

            String loteNumber = cards.getFirst().getLoteNumber();

            UserEntity userEntity = userEntityRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            checkIfBatchAlreadyExists(loteNumber);

            CardBatchEntity batch = CardBatchEntity.builder()
                    .loteNumber(loteNumber)
                    .totalCards(cards.size())
                    .user(userEntity)
                    .status(CardBatchEntity.BatchStatus.PROCESSING)
                    .build();

            cardBatchEntityRepository.save(batch);

            int successCount = 0;

            for (CardRequestDTO cardRequest : cards) {
                successCount = createCard(loteNumber, cardRequest, userEntity, successCount);
            }

            batch.setProcessedCards(successCount);
            batch.setStatus(CardBatchEntity.BatchStatus.COMPLETED);
            cardBatchEntityRepository.save(batch);

            log.info("Batch processing completed. Processed {} out of {} cards", successCount, cards.size());
            return BatchUploadResponseDTO.builder()
                    .loteNumber(loteNumber)
                    .totalCards(cards.size())
                    .processedCards(successCount)
                    .status("COMPLETED")
                    .build();
        } catch (Exception e) {
            log.error("Error uploading batch for user: {}: {}", username, e.getMessage());
            return BatchUploadResponseDTO.builder()
                            .errorMessage(e.getMessage())
                            .status("FAILED")
                            .build();
        }
    }

    private int createCard(String loteNumber, CardRequestDTO cardRequest, UserEntity userEntity, int successCount) {
        try {
            CardEntity cardEntity = cardEntityMapper.fromCardRequestDTO(cardRequest);
            cardEntity.setLoteNumber(loteNumber);
            cardEntity.setUser(userEntity);

            boolean cardNumberOrIdentifierAlreadyExists = cardValidate(cardEntity);

            if(cardNumberOrIdentifierAlreadyExists) {
                log.warn("CardNumber with  identifier: {} and encryptedCardNumber: {} already exists", cardRequest.getCardIdentifier(), cardEntity.getCardNumberEncrypted());
            }

            if (!cardNumberOrIdentifierAlreadyExists) {
                cardEntityRepository.save(cardEntity);
                successCount++;
            }
        } catch (Exception e) {
            log.error("Error processing card {}: {}", cardRequest.getCardIdentifier(), e.getMessage());
        }

        return successCount;
    }

    private void checkIfBatchAlreadyExists(String loteNumber) {
        if (cardBatchEntityRepository.existsByLoteNumber(loteNumber)) {
            log.warn("Batch with lote number already exists: {}", loteNumber);
            throw new BatchNumberAlreadyExistsException("Batch with this lote number already exists");
        }
    }

    private boolean cardValidate(CardEntity cardEntity) {
        return cardEntityRepository.existsByCardIdentifier(cardEntity.getCardIdentifier())
                || cardEntityRepository.existsByCardNumberEncrypted(cardEntity.getCardNumberEncrypted());
    }

    public CardResponseDTO getCard(Long cardId, String username) {
        log.debug("Fetching card {} for user: {}", cardId, username);

        CardEntity cardEntity = cardEntityRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(CARD_BY_ID_NOT_FOUND_ERROR_MESSAGE.formatted(cardId)));

        if (!cardEntity.getUser().getUsername().equals(username)) {
            log.warn("Unauthorized access attempt to card {} by user: {}", cardId, username);
            throw new UserNotAuthorizedException(USER_NOT_AUTHORIZED_TO_ACCESS_THIS_CARD_ERROR_MESSAGE.formatted(username, cardId));
        }

        return cardEntityMapper.toCardResponseDTO(cardEntity);
    }

    public CardResponseDTO searchCardByCardNumber(String cardNumber, String username) {
        UserEntity userEntity = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_WITH_USERNAME_ERROR_MESSAGE.formatted(username)));
        String encrypt = cryptoService.encrypt(cardNumber);
        log.debug("Searching card with last digits: {} for user: {}", encrypt, username);

        CardResponseDTO response = cardEntityMapper.toCardResponseDTO(cardEntityRepository.findByUserIdAndCardNumberEncrypted(userEntity.getId(), encrypt)
                .orElseThrow(() -> new CardNotFoundException(CARD_BY_ID_ENCRYPT_FOUND_ERROR_MESSAGE.formatted(encrypt))));

        log.debug("Finishing search card with last digits: {} for user: {} - response: {}", cardNumber, username, response);
        return response;
    }

}

