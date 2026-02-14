package com.hyperativa.api.service;

import com.hyperativa.api.dto.CardRequestDTO;
import com.hyperativa.api.dto.CardResponseDTO;
import com.hyperativa.api.entity.CardEntity;
import com.hyperativa.api.entity.UserEntity;
import com.hyperativa.api.exception.CardAlreadyExistsException;
import com.hyperativa.api.exception.CardNotFoundException;
import com.hyperativa.api.exception.UserNotAuthorizedException;
import com.hyperativa.api.mapper.CardEntityMapper;
import com.hyperativa.api.repository.CardEntityRepository;
import com.hyperativa.api.repository.UserEntityRepository;
import com.hyperativa.api.security.CryptoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardEntityRepository cardEntityRepository;

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private CryptoService cryptoService;

    @Mock
    private CardEntityMapper cardEntityMapper;

    @InjectMocks
    private CardService cardService;

    @Test
    void testCreateCardSuccess() {
        String username = "user1";
        CardRequestDTO request = new CardRequestDTO();
        request.setCardIdentifier("id1");
        request.setCardNumber("4456897999999999");

        UserEntity user = UserEntity.builder().id(1L).username(username).build();
        CardEntity entity = new CardEntity();
        entity.setId(100L);
        entity.setCardIdentifier("id1");

        CardResponseDTO expectedResponse = CardResponseDTO.builder().id(100L).cardIdentifier("id1").build();

        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardEntityMapper.fromCardRequestDTO(request)).thenReturn(entity);
        when(cardEntityRepository.existsByCardIdentifier(entity.getCardIdentifier())).thenReturn(false);
        when(cardEntityRepository.save(entity)).thenReturn(entity);
        when(cardEntityMapper.toCardResponseDTO(entity)).thenReturn(expectedResponse);

        CardResponseDTO response = cardService.createCard(request, username);

        assertNotNull(response);
        assertThat(response, equalTo(expectedResponse));
        verify(cardEntityRepository, times(1)).save(entity);
    }

    @Test
    void testCreateCardSuccessWhenCardIdentifierAlreadyExistsShouldReturn() {
        String username = "user1";
        CardRequestDTO request = new CardRequestDTO();
        request.setCardIdentifier("id1");
        request.setCardNumber("4456897999999999");

        UserEntity user = UserEntity.builder().id(1L).username(username).build();
        CardEntity entity = new CardEntity();
        entity.setId(100L);
        entity.setCardIdentifier("id1");

        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardEntityMapper.fromCardRequestDTO(request)).thenReturn(entity);
        when(cardEntityRepository.existsByCardIdentifier(entity.getCardIdentifier())).thenReturn(true);

        assertThrows(CardAlreadyExistsException.class, () -> cardService.createCard(request, username));
    }

    @Test
    void testGetCardUnauthorized() {
        String username = "user1";
        UserEntity owner = UserEntity.builder().id(2L).username("other").build();
        CardEntity card = new CardEntity();
        card.setId(10L);
        card.setUser(owner);

        when(cardEntityRepository.findById(10L)).thenReturn(Optional.of(card));

        assertThrows(UserNotAuthorizedException.class, () -> cardService.getCard(10L, username));
    }

    @Test
    void testGetCardAuthorized() {
        String username = "user1";
        UserEntity owner = UserEntity.builder().id(1L).username(username).build();
        CardEntity card = new CardEntity();
        card.setId(10L);
        card.setUser(owner);

        CardResponseDTO expectedResponse = CardResponseDTO.builder().id(100L).cardIdentifier("id1").build();

        when(cardEntityRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cardEntityMapper.toCardResponseDTO(any())).thenReturn(expectedResponse);

        CardResponseDTO response = cardService.getCard(10L, username);

        assertNotNull(response);
        assertThat(response, equalTo(expectedResponse));
    }

    @Test
    void searchCardByCardNumberNotFound() {
        String username = "user1";
        UserEntity user = UserEntity.builder().id(1L).username(username).build();

        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cryptoService.encrypt("4456897999999999")).thenReturn("encrypted-value");
        when(cardEntityRepository.findByUserIdAndCardNumberEncrypted(user.getId(), "encrypted-value")).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.searchCardByCardNumber("4456897999999999", username));
    }

    @Test
    void searchCardByCardNumber() {
        String username = "user1";
        UserEntity user = UserEntity.builder().id(1L).username(username).build();
        CardEntity card = new CardEntity();
        card.setId(10L);
        card.setUser(user);

        CardResponseDTO expectedResponse = CardResponseDTO.builder().id(100L).cardIdentifier("id1").build();

        when(cardEntityRepository.findByUserIdAndCardNumberEncrypted(any(), any())).thenReturn(Optional.of(card));
        when(cardEntityMapper.toCardResponseDTO(any())).thenReturn(expectedResponse);
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cryptoService.encrypt(any())).thenReturn("encrypted-value");

        CardResponseDTO response = cardService.searchCardByCardNumber("4456897999999999", username);

        assertNotNull(response);
        assertThat(response, equalTo(expectedResponse));
    }

}
