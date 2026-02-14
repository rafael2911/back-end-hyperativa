package com.hyperativa.api.util;

import com.hyperativa.api.dto.CardRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FileParserServiceTest {

    @Autowired
    private FileParserService fileParserService;

    @Test
    void testParseValidCardFile() {
        String fileContent = "DESAFIO-HYPERATIVA           20180524LOTE0001000010\n" +
                "C2     4456897999999999                               \n" +
                "C1     4456897922969999                               \n" +
                "C3     4456897999999999\n" +
                "LOTE0001000010                                        \n";

        List<CardRequestDTO> cards = fileParserService.parseCardFile(fileContent);

        assertNotNull(cards);
        assertEquals(3, cards.size());
        assertEquals("C2", cards.get(0).getCardIdentifier());
        assertEquals("4456897999999999", cards.get(0).getCardNumber());
    }

    @Test
    void testParseFileWithEmptyLines() {
        String fileContent = "DESAFIO-HYPERATIVA           20180524LOTE0001000010\n" +
                "\n" +
                "C2     4456897999999999                               \n" +
                "\n" +
                "C1     4456897922969999                               \n" +
                "LOTE0001000010                                        \n";

        List<CardRequestDTO> cards = fileParserService.parseCardFile(fileContent);

        assertNotNull(cards);
        assertEquals(2, cards.size());
    }

    @Test
    void testParseFileWithInvalidLines() {
        String fileContent = "DESAFIO-HYPERATIVA           20180524LOTE0001000010\n" +
                "C2     4456897999999999                               \n" +
                "INVALID LINE\n" +
                "C1     4456897922969999                               \n" +
                "LOTE0001000010                                        \n";

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
        String fileContent = "DESAFIO-HYPERATIVA           20180524LOTE0001000010\n" +
                "C2     4456897999999999                               \n" +
                "C1     4456897922969999                               \n" +
                "LOTE0001000010                                        \n";

        List<CardRequestDTO> cards = fileParserService.parseCardFile(fileContent);

        assertNotNull(cards);
        assertEquals("LOTE0001", cards.get(0).getLoteNumber());
    }
}

