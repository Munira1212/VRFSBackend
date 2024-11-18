package com.example.vrfsbackend.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class NasaWaterQualityService {


        private final String NASA_API_URL = "https://n5eil02u.ecs.nsidc.org/egi/request";

        @Value("${nasa.api.username}")
        private String username;

        @Value("${nasa.api.password}")
        private String password;

        public String fetchWaterQualityData(String startDate, String endDate, String boundingBox) {
            RestTemplate restTemplate = new RestTemplate();

            String url = UriComponentsBuilder.fromHttpUrl(NASA_API_URL)
                    .queryParam("short_name", "GLDAS_NOAH025_3H")
                    .queryParam("version", "2.1")
                    .queryParam("time_start", startDate)
                    .queryParam("time_end", endDate)
                    .queryParam("bbox", boundingBox)
                    .queryParam("format", "CSV")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to fetch data from NASA API. Status: " + response.getStatusCode());
            }
        }
    }


