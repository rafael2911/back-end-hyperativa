package com.hyperativa.api.mapper;

import com.hyperativa.api.dto.CardRequestDTO;
import com.hyperativa.api.entity.CardBatchEntity;
import com.hyperativa.api.service.FileParserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Mapper(componentModel = "spring")
public abstract class CardBatchEntityMapper {

    @Autowired
    protected FileParserService fileParserService;

    @Mapping(target = "loteNumber", source = "source", qualifiedByName = "extractBatchNumberFromFile")
    public abstract CardBatchEntity fromBatchUploadRequestDTO(MultipartFile source);

    @Named("extractBatchNumberFromFile")
    protected String extractBatchNumberFromFile(MultipartFile file) throws IOException {
        String fileContent = new String(file.getBytes());
        CardRequestDTO card = fileParserService.parseCardFile(fileContent).getFirst();
        return card.getLoteNumber();
    }

}
