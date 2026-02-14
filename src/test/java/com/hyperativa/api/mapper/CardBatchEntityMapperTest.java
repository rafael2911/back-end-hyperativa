package com.hyperativa.api.mapper;

import com.hyperativa.api.dto.BatchUploadRequestDTO;
import com.hyperativa.api.dto.CardRequestDTO;
import com.hyperativa.api.entity.CardBatchEntity;
import com.hyperativa.api.util.FileParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardBatchEntityMapperTest {

    @Mock
    private FileParserService fileParserService;

    private CardBatchEntityMapper cardBatchEntityMapper;

    @BeforeEach
    void setUp() {
        cardBatchEntityMapper = Mappers.getMapper(CardBatchEntityMapper.class);

        ReflectionTestUtils.setField(cardBatchEntityMapper, "fileParserService", fileParserService);
    }

    @Test
    void testFromBatchUploadRequestDTOWithValidFile() {
        MockMultipartFile file = new MockMultipartFile("file", "cards.txt", "text/plain", "file content".getBytes());

        CardRequestDTO card1 = CardRequestDTO.builder()
                .cardIdentifier("C1")
                .cardNumber("4456897000000001")
                .loteNumber("LOTE001")
                .build();

        List<CardRequestDTO> cards = new LinkedList<>();
        cards.add(card1);

        when(fileParserService.parseCardFile(anyString())).thenReturn(cards);

        BatchUploadRequestDTO request = BatchUploadRequestDTO.builder().file(file).build();

        CardBatchEntity entity = cardBatchEntityMapper.fromBatchUploadRequestDTO(request);

        assertNotNull(entity);
        assertEquals("LOTE001", entity.getLoteNumber());
        verify(fileParserService, times(1)).parseCardFile(anyString());
    }

    @Test
    void testFromBatchUploadRequestDTOWithMultipleCards() {
        MockMultipartFile file = new MockMultipartFile("file", "cards.txt", "text/plain", "file content".getBytes());

        CardRequestDTO card1 = CardRequestDTO.builder()
                .cardIdentifier("C1")
                .cardNumber("4456897000000001")
                .loteNumber("LOTE002")
                .build();

        CardRequestDTO card2 = CardRequestDTO.builder()
                .cardIdentifier("C2")
                .cardNumber("4456897000000002")
                .loteNumber("LOTE002")
                .build();

        List<CardRequestDTO> cards = new LinkedList<>();
        cards.add(card1);
        cards.add(card2);

        when(fileParserService.parseCardFile(anyString())).thenReturn(cards);

        BatchUploadRequestDTO request = BatchUploadRequestDTO.builder().file(file).build();

        CardBatchEntity entity = cardBatchEntityMapper.fromBatchUploadRequestDTO(request);

        assertNotNull(entity);
        assertEquals("LOTE002", entity.getLoteNumber());
        // Should extract loteNumber from first card
        verify(fileParserService, times(1)).parseCardFile(anyString());
    }

    @Test
    void testFromBatchUploadRequestDTOExtractLoteNumberFromFirstCard() {
        MockMultipartFile file = new MockMultipartFile("file", "batch.txt", "text/plain", "batch data".getBytes());

        CardRequestDTO card = CardRequestDTO.builder()
                .cardIdentifier("BATCH")
                .cardNumber("1234567890123456")
                .loteNumber("BATCH_LOTE_123")
                .build();

        List<CardRequestDTO> cards = new LinkedList<>();
        cards.add(card);

        when(fileParserService.parseCardFile("batch data")).thenReturn(cards);

        BatchUploadRequestDTO request = BatchUploadRequestDTO.builder().file(file).build();

        CardBatchEntity entity = cardBatchEntityMapper.fromBatchUploadRequestDTO(request);

        assertNotNull(entity);
        assertEquals("BATCH_LOTE_123", entity.getLoteNumber());
    }
}
