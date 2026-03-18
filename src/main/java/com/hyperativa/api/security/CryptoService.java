package com.hyperativa.api.security;

public interface CryptoService {

    String encrypt(String value);

    String decrypt(String encryptedValue);

}
