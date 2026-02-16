package com.hyperativa.api.security;

import com.hyperativa.api.exception.DecryptException;
import com.hyperativa.api.exception.EncryptException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CryptoServiceTest {

    @InjectMocks
    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cryptoService, "secretKey", "test-secret-key-value");
    }

    @Test
    void testEncryptAndDecrypt() {
        String originalValue = "4456897999999999";

        String encrypted = cryptoService.encrypt(originalValue);
        assertNotNull(encrypted);
        assertNotEquals(originalValue, encrypted);

        String decrypted = cryptoService.decrypt(encrypted);
        assertEquals(originalValue, decrypted);
    }

    @Test
    void testEncryptDifferentInputsProduceDifferentOutputs() {
        String value1 = "4456897999999999";
        String value2 = "4456897922969999";

        String encrypted1 = cryptoService.encrypt(value1);
        String encrypted2 = cryptoService.encrypt(value2);

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void testEncryptWhenNullValueShouldThrowEncryptException() {


        assertThrows(EncryptException.class,
                () -> cryptoService.encrypt(null));
    }

    @Test
    void testDecryptWithInvalidBase64ThrowsDecryptException() {
        assertThrows(DecryptException.class, () -> {
            cryptoService.decrypt("invalid-base64-!@#$%");
        });
    }

    @Test
    void testDifferentSecretKeysProduceDifferentCiphertext() {
        String original = "4456897999999999";

        // encrypt with default secret
        String encryptedDefault = cryptoService.encrypt(original);

        // change secret key via reflection and encrypt again
        CryptoService another = new CryptoService();
        ReflectionTestUtils.setField(another, "secretKey", "another-secret-key-value");
        String encryptedWithAnother = another.encrypt(original);

        assertNotNull(encryptedWithAnother);
        assertNotEquals(encryptedDefault, encryptedWithAnother);

        // decrypt both with their own service instances
        String decDefault = cryptoService.decrypt(encryptedDefault);
        assertEquals(original, decDefault);

        String decAnother = another.decrypt(encryptedWithAnother);
        assertEquals(original, decAnother);
    }
}
