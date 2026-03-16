package io.github.thirumalx.crypto;

import org.junit.jupiter.api.Test;

public class GenerateKey {
    
    @Test
    public void generateBase64Key() {
        // Generate a random 256-bit (32-byte) key and print it in Base64
        byte[] keyBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(keyBytes);
        String base64Key = java.util.Base64.getEncoder().encodeToString(keyBytes);
        System.out.println("Generated Base64 Key: " + base64Key);
    }
}
