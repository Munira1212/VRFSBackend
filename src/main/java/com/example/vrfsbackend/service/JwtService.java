package com.example.vrfsbackend.service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.Date;
@Service
public class JwtService {
/*
    private final KeyLoader keyLoader;

    @Autowired
    public JwtService(KeyLoader keyLoader) {
        this.keyLoader = keyLoader;
    }

    public String generateJwt() throws Exception {
        // Load the private key using KeyLoader
        PrivateKey privateKey = keyLoader.loadPrivateKeyFromResources();

        return Jwts.builder()
                .setIssuer("b0525342eff0a977b0477fd881140426")
                .setSubject("n00hj5gm")
                .setAudience("https://land.copernicus.eu/@@oauth2-token")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 300000))
                .setId("unique-jwt-id")
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }*/
}
