package com.example.vrfsbackend.service;

import org.junit.jupiter.api.Test;

import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.*;

class KeyLoaderTest {



    @Test
    void testLoadPrivateKey() {
        try {
            KeyLoader keyLoader = new KeyLoader();
            PrivateKey privateKey = keyLoader.loadPrivateKeyFromResources();
            assertNotNull(privateKey);
            System.out.println("Private Key Loaded Successfully");
        } catch (Exception e) {
            fail("Failed to load private key: " + e.getMessage());
        }
    }
}