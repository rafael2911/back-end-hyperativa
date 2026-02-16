package com.hyperativa.api.service;

import com.hyperativa.api.dto.CardRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class FileParserServiceTest {

    @InjectMocks
    private FileParserService fileParserService;

    @Test
    void testParseValidCardFile() {
        String fileContent = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000010
                C2     4456897999999999                               
                C1     4456897922969999                               
                C3     4456897999999999
                LOTE0001000010                                        
                """;

        List<CardRequestDTO> cards = fileParserService.parseCardFile(fileContent);

        assertNotNull(cards);
        assertEquals(3, cards.size());
        assertEquals("C2", cards.getFirst().getCardIdentifier());
        assertEquals("4456897999999999", cards.getFirst().getCardNumber());
    }

    @Test
    void testParseFileWithEmptyLines() {
        String fileContent = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000010
                
                C2     4456897999999999                               
                
                C1     4456897922969999                               
                LOTE0001000010                                        
                """;

        List<CardRequestDTO> cards = fileParserService.parseCardFile(fileContent);

        assertNotNull(cards);
        assertEquals(2, cards.size());
    }

    @Test
    void testParseFileWithInvalidLines() {
        String fileContent = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000010
                C2     4456897999999999                               
                INVALID LINE
                C1     4456897922969999                               
                LOTE0001000010                                        
                """;

        List<CardRequestDTO> cards = fileParserService.parseCardFile(fileContent);

        assertNotNull(cards);
        assertEquals(2, cards.size());
    }

    @Test
    void testParseEmptyFile() {
        String fileContent = "";

        List<CardRequestDTO> cards = fileParserService.parseCardFile(fileContent);

        assertNotNull(cards);
        assertEquals(0, cards.size());
    }

    @Test
    void testParseFileLoteNumberExtraction() {
        String fileContent = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000010
                C2     4456897999999999                               
                C1     4456897922969999                               
                LOTE0001000010                                        
                """;

        List<CardRequestDTO> cards = fileParserService.parseCardFile(fileContent);

        assertNotNull(cards);
        assertEquals("LOTE0001", cards.get(0).getLoteNumber());
    }
}

