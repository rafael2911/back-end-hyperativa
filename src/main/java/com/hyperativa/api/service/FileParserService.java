package com.hyperativa.api.service;

import com.hyperativa.api.dto.CardRequestDTO;

import java.util.List;

public interface FileParserService {

    List<CardRequestDTO> parseCardFile(String fileContent);

}
