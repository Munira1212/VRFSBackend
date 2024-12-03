package com.example.vrfsbackend.controller;
import com.example.vrfsbackend.service.JwtService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/jwt")
public class JwtController {

   /* private final JwtService jwtService;

    public JwtController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/generate")
    public String generateJwt() {
        try {
            return jwtService.generateJwt();
        } catch (Exception e) {
            return "Error generating JWT: " + e.getMessage();
        }
    }

    @PostMapping("/request-token")
    public ResponseEntity<String> requestAccessToken() {
        try {
            // Generate the JWT token
            String generatedJwt = jwtService.generateJwt();

            // Set up headers for the request
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + generatedJwt);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Set up the body for the request
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");

            // Create an entity containing headers and body
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            // Set up RestTemplate and make the POST request
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://land.copernicus.eu/@@oauth2-token",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Return the response body
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error requesting access token: " + e.getMessage());
        }
    }*/
}

