package com.example.vrfsbackend.service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class KeyLoader {

    // Method to load the private key from the resources folder using ClassLoader
    public PrivateKey loadPrivateKeyFromResources() throws Exception {
        InputStream inputStream = null;
        try {
            // Load the private key file using ClassLoader
            inputStream = getClass().getClassLoader().getResourceAsStream("private_key_pkcs8.pem");

            if (inputStream == null) {
                throw new FileNotFoundException("Could not find private key file: private_key_pkcs8.pem");
            }

            // Read the content of the private key file
            String privateKeyPEM = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Debugging: Print part of the private key (first 50 characters) for verification
            System.out.println("Private Key (PEM) preview: " + privateKeyPEM.substring(0, Math.min(50, privateKeyPEM.length())) + "...");

            // Clean up the key content and convert it to a PrivateKey object
            return convertToPrivateKey(cleanPrivateKey(privateKeyPEM));
        } finally {
            if (inputStream != null) {
                inputStream.close(); // Ensure the InputStream is properly closed
            }
        }
    }

    // Method to clean the private key
    private String cleanPrivateKey(String key) {
        String cleanedKey = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("[\\r\\n]", "") // Remove all newlines and carriage returns
                .trim(); // Remove leading and trailing spaces

        // Debugging: Print the cleaned key length
        System.out.println("Cleaned Key Length: " + cleanedKey.length());
        return cleanedKey;
    }

    // Method to convert cleaned Base64 key to PrivateKey object
    private PrivateKey convertToPrivateKey(String cleanedKey) throws Exception {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(cleanedKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (IllegalArgumentException e) {
            throw new Exception("Failed to decode Base64 key. Please verify the key formatting. Error: " + e.getMessage(), e);
        }
    }
}
