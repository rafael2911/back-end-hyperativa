package com.hyperativa.api.service;

import com.hyperativa.api.dto.BatchUploadRequestDTO;
import com.hyperativa.api.dto.BatchUploadResponseDTO;
import com.hyperativa.api.dto.CardRequestDTO;
import com.hyperativa.api.entity.CardBatchEntity;
import com.hyperativa.api.entity.CardEntity;
import com.hyperativa.api.entity.UserEntity;
import com.hyperativa.api.mapper.CardEntityMapper;
import com.hyperativa.api.repository.CardBatchEntityRepository;
import com.hyperativa.api.repository.CardEntityRepository;
import com.hyperativa.api.repository.UserEntityRepository;
import com.hyperativa.api.util.FileParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceBatchTest {

    @Mock
    private CardEntityRepository cardEntityRepository;

    @Mock
    private CardBatchEntityRepository cardBatchEntityRepository;

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private CardEntityMapper cardEntityMapper;

    @Mock
    private FileParserService fileParserService;

    @InjectMocks
    private CardService cardService;

    private BatchUploadRequestDTO request;

    @BeforeEach
    void setUp() {
        MockMultipartFile file = new MockMultipartFile("file", "cards.txt", "text/plain", "dummy".getBytes());
        request = BatchUploadRequestDTO.builder().file(file).build();
    }

    @Test
    void testCreateCardsFromBatchShouldReturnCompleted() {
        String username = "user1";

        CardRequestDTO c1 = CardRequestDTO.builder().cardIdentifier("C1").cardNumber("4456897000000001").loteNumber("L1").build();
        CardRequestDTO c2 = CardRequestDTO.builder().cardIdentifier("C2").cardNumber("4456897000000002").loteNumber("L1").build();
        CardRequestDTO c3 = CardRequestDTO.builder().cardIdentifier("C3").cardNumber("4456897000000003").loteNumber("L1").build();
        List<CardRequestDTO> cards = new LinkedList<>();
        cards.add(c1);
        cards.add(c2);
        cards.add(c3);

        UserEntity user = UserEntity.builder().id(1L).username(username).build();

        when(fileParserService.parseCardFile(anyString())).thenReturn(cards);
        when(userEntityRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));
        when(cardBatchEntityRepository.existsByLoteNumber("L1")).thenReturn(false);

        // mapping and saving cards
        CardEntity e1 = new CardEntity();
        e1.setCardIdentifier("C1");
        CardEntity e2 = new CardEntity();
        e2.setCardIdentifier("C2");
        CardEntity e3 = new CardEntity();
        e2.setCardIdentifier("C3");
        when(cardEntityMapper.fromCardRequestDTO(c1)).thenReturn(e1);
        when(cardEntityMapper.fromCardRequestDTO(c2)).thenReturn(e2);
        when(cardEntityMapper.fromCardRequestDTO(c3)).thenReturn(e3);

        when(cardEntityRepository.existsByCardIdentifier(e1.getCardIdentifier())).thenReturn(false);
        when(cardEntityRepository.existsByCardIdentifier(e2.getCardIdentifier())).thenReturn(false);
        when(cardEntityRepository.existsByCardIdentifier(e3.getCardIdentifier())).thenReturn(true);
        when(cardEntityRepository.save(any(CardEntity.class))).thenAnswer(i -> i.getArgument(0));

        BatchUploadResponseDTO resp = cardService.createCardsFromBatch(request, username);

        assertNotNull(resp);
        assertEquals("COMPLETED", resp.getStatus());
        assertEquals(2, resp.getProcessedCards());
        assertEquals(3, resp.getTotalCards());
        assertEquals("L1", resp.getLoteNumber());

        verify(cardBatchEntityRepository, times(2)).save(any(CardBatchEntity.class)); // initial save + final save
        verify(cardEntityRepository, times(2)).save(any(CardEntity.class));
    }

    @Test
    void testCreateCardsFromBatchWhenExceptionShouldReturnCompleted() {
        String username = "user1";

        CardRequestDTO c1 = CardRequestDTO.builder().cardIdentifier("C1").cardNumber("4456897000000001").loteNumber("L1").build();
        List<CardRequestDTO> cards = new LinkedList<>();
        cards.add(c1);

        UserEntity user = UserEntity.builder().id(1L).username(username).build();

        when(fileParserService.parseCardFile(anyString())).thenReturn(cards);
        when(userEntityRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));
        when(cardBatchEntityRepository.existsByLoteNumber("L1")).thenReturn(false);

        // mapping and saving cards
        CardEntity e1 = new CardEntity();
        e1.setCardIdentifier("C1");
        when(cardEntityMapper.fromCardRequestDTO(c1)).thenThrow(RuntimeException.class);

        BatchUploadResponseDTO resp = cardService.createCardsFromBatch(request, username);

        assertNotNull(resp);
        assertEquals("COMPLETED", resp.getStatus());
        assertEquals(0, resp.getProcessedCards());
        assertEquals(1, resp.getTotalCards());
        assertEquals("L1", resp.getLoteNumber());

        verify(cardBatchEntityRepository, times(2)).save(any(CardBatchEntity.class)); // initial save + final save
        verify(cardEntityRepository, times(0)).save(any(CardEntity.class));
    }

    @Test
    void testCreateCardsFromBatchWhenNoValidCardsShouldReturnFailed() {
        String username = "user1";

        when(fileParserService.parseCardFile(anyString())).thenReturn(new LinkedList<>());

        BatchUploadResponseDTO resp = cardService.createCardsFromBatch(request, username);

        assertNotNull(resp);
        assertEquals("FAILED", resp.getStatus());
        assertEquals("No valid cards found in file", resp.getErrorMessage());
    }

    @Test
    void testCreateCardsFromBatchWhenLoteAlreadyExistsShouldReturnFailed() {
        String username = "user1";

        CardRequestDTO c1 = CardRequestDTO.builder().cardIdentifier("C1").cardNumber("4456897000000001").loteNumber("L2").build();
        List<CardRequestDTO> cards = new LinkedList<>();
        cards.add(c1);

        UserEntity user = UserEntity.builder().id(1L).username(username).build();

        when(fileParserService.parseCardFile(anyString())).thenReturn(cards);
        when(userEntityRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));
        when(cardBatchEntityRepository.existsByLoteNumber("L2")).thenReturn(true);

        BatchUploadResponseDTO resp = cardService.createCardsFromBatch(request, username);

        assertNotNull(resp);
        assertEquals("FAILED", resp.getStatus());
        assertTrue(resp.getErrorMessage().contains("Batch with this lote number already exists"));
    }
}
