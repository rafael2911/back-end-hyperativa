package com.hyperativa.api.security;

import com.hyperativa.api.exception.DecryptException;
import com.hyperativa.api.exception.EncryptException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
@Component
public class CryptoService {

    private static final String ALGORITHM = "AES";

    @Value("${app.crypto.secret-key}")
    private String secretKey;

    private SecretKeySpec getKey() throws Exception {
        byte[] decodedKey = new byte[16]; // 128-bit key
        byte[] encodedKey = secretKey.getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        encodedKey = md.digest(encodedKey);
        System.arraycopy(encodedKey, 0, decodedKey, 0, Math.min(encodedKey.length, 16));
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    public String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            byte[] encryptedValue = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        } catch (Exception e) {
            log.error("Error encrypting value", e);
            throw new EncryptException("Error encrypting value");
        }
    }

    public String decrypt(String encryptedValue) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            byte[] decodedValue = Base64.getDecoder().decode(encryptedValue);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting value", e);
            throw new DecryptException("Error decrypting value");
        }
    }
}

