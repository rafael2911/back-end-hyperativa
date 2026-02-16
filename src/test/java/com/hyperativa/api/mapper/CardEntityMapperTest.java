package com.hyperativa.api.mapper;

import com.hyperativa.api.dto.CardRequestDTO;
import com.hyperativa.api.dto.CardResponseDTO;
import com.hyperativa.api.entity.CardEntity;
import com.hyperativa.api.security.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardEntityMapperTest {

    @Mock
    private CryptoService cryptoService;

    private CardEntityMapper cardEntityMapper;

    @BeforeEach
    void setUp() {
        cardEntityMapper = Mappers.getMapper(CardEntityMapper.class);

        ReflectionTestUtils.setField(cardEntityMapper, "cryptoService", cryptoService);
    }

    @Test
    void testFromCardRequestDTOWithEncryption() {
        CardRequestDTO request = CardRequestDTO.builder()
                .cardNumber("4456897999999999")
                .cardIdentifier("ID001")
                .loteNumber("L001")
                .build();

        when(cryptoService.encrypt("4456897999999999")).thenReturn("encrypted-card-number");

        CardEntity entity = cardEntityMapper.fromCardRequestDTO(request);

        assertNotNull(entity);
        assertEquals("ID001", entity.getCardIdentifier());
        assertEquals("encrypted-card-number", entity.getCardNumberEncrypted());
        assertEquals("9999", entity.getCardNumberLastDigits());
        verify(cryptoService, times(1)).encrypt("4456897999999999");
    }

    @Test
    void testToCardResponseDTO() {
        CardEntity entity = new CardEntity();
        entity.setId(100L);
        entity.setCardIdentifier("ID001");
        entity.setCardNumberEncrypted("encrypted-value");
        entity.setCardNumberLastDigits("9999");

        CardResponseDTO response = cardEntityMapper.toCardResponseDTO(entity);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("ID001", response.getCardIdentifier());
        assertEquals("9999", response.getCardNumberLastDigits());
    }

    @Test
    void testFromCardRequestDTOWithDifferentCardNumbers() {
        CardRequestDTO request1 = CardRequestDTO.builder()
                .cardNumber("4456897000000001")
                .cardIdentifier("ID001")
                .build();

        CardRequestDTO request2 = CardRequestDTO.builder()
                .cardNumber("5555666677778888")
                .cardIdentifier("ID002")
                .build();

        when(cryptoService.encrypt("4456897000000001")).thenReturn("enc-1");
        when(cryptoService.encrypt("5555666677778888")).thenReturn("enc-2");

        CardEntity entity1 = cardEntityMapper.fromCardRequestDTO(request1);
        CardEntity entity2 = cardEntityMapper.fromCardRequestDTO(request2);

        assertNotNull(entity1);
        assertNotNull(entity2);
        assertEquals("0001", entity1.getCardNumberLastDigits());
        assertEquals("8888", entity2.getCardNumberLastDigits());
        assertNotEquals(entity1.getCardNumberEncrypted(), entity2.getCardNumberEncrypted());
    }

    @Test
    void testGetLastDigitsWithShortCardNumber() {
        CardRequestDTO request = CardRequestDTO.builder()
                .cardNumber("1234")
                .cardIdentifier("SHORT")
                .build();

        when(cryptoService.encrypt("1234")).thenReturn("enc-short");

        CardEntity entity = cardEntityMapper.fromCardRequestDTO(request);

        assertNotNull(entity);
        assertEquals("1234", entity.getCardNumberLastDigits());
    }
}
