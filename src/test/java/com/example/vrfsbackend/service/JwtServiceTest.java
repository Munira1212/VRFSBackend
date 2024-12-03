package com.example.vrfsbackend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {


    @Test
    void generateJwt() {
        try {
            // Create a KeyLoader instance manually for the test
            KeyLoader keyLoader = new KeyLoader();

            // Create JwtService with the KeyLoader instance
            JwtService jwtService = new JwtService(keyLoader);

            // Generate the JWT
            String jwt = jwtService.generateJwt();

            // Assert that the JWT is not null
            assertNotNull(jwt, "JWT should not be null");
            System.out.println("Generated JWT: " + jwt);

        } catch (Exception e) {
            // Fail the test if an exception occurs
            fail("Failed to generate JWT: " + e.getMessage());
        }
    }


}