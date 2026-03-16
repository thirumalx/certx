package io.github.thirumalx.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Encrypts/decrypts sensitive certificate passwords before storage.
 */
@Service
public class PasswordCryptoService {

    private static final String KEY_ALGO = "AES";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;
    private static final String VERSION_PREFIX = "v1";

    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordCryptoService(@Value("${certx.crypto.password-key}") String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalStateException("Missing certx.crypto.password-key (Base64-encoded 16/24/32-byte key)");
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalStateException(
                    "Invalid certx.crypto.password-key length. Expected 16/24/32 bytes after Base64 decode.");
        }
        this.key = new SecretKeySpec(keyBytes, KEY_ALGO);
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return VERSION_PREFIX + ":" + Base64.getEncoder().encodeToString(iv) + ":"
                    + Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt certificate password", e);
        }
    }

    public String decrypt(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (!value.startsWith(VERSION_PREFIX + ":")) {
            // Backward compatibility for existing plaintext records.
            return value;
        }
        String[] parts = value.split(":", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid encrypted password format");
        }
        try {
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] cipherText = Base64.getDecoder().decode(parts[2]);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plainBytes = cipher.doFinal(cipherText);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt certificate password", e);
        }
    }
}
