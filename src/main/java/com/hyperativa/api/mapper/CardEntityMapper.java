package com.hyperativa.api.mapper;

import com.hyperativa.api.dto.CardRequestDTO;
import com.hyperativa.api.dto.CardResponseDTO;
import com.hyperativa.api.entity.CardEntity;
import com.hyperativa.api.security.CryptoService;
import com.hyperativa.api.util.CardNumberUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CardEntityMapper {

    @Autowired
    protected CryptoService cryptoService;

    public abstract CardResponseDTO toCardResponseDTO(CardEntity source);

    @Mapping(target = "cardNumberEncrypted", source = "cardNumber", qualifiedByName = "encryptCardNumber")
    @Mapping(target = "cardNumberLastDigits", source = "cardNumber", qualifiedByName = "getLastDigits")
    public abstract CardEntity fromCardRequestDTO(CardRequestDTO source);

    @Named("encryptCardNumber")
    protected String encryptCardNumber(String cardNumber) {
        return cryptoService.encrypt(cardNumber);
    }

    @Named("getLastDigits")
    protected String getLastDigits(String cardNumber) {
        return CardNumberUtils.getLastDigits(cardNumber);
    }

}
