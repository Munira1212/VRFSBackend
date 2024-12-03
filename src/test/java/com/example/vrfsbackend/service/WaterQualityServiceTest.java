package com.example.vrfsbackend.service;

import com.example.vrfsbackend.model.WaterQuality;
import com.example.vrfsbackend.repository.WaterQualityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WaterQualityServiceTest {

    @Mock
    private WaterQualityRepository waterQualityRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WaterQualityService waterQualityService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAssessWaterQuality() {
        // Arrange: Set up mock responses
        double latitude = -1.2921; // Latitude for Nairobi, Kenya
        double longitude = 36.8219; // Longitude for Nairobi, Kenya

        // Mock air quality response from OpenWeatherMap API
        Map<String, Object> airQualityMockResponse = new HashMap<>();
        Map<String, Double> components = new HashMap<>();
        components.put("pm10", 20.0); // Example PM10 value
        airQualityMockResponse.put("components", components);
        Map<String, Object> listEntry = new HashMap<>();
        listEntry.put("components", components);
        airQualityMockResponse.put("list", new Object[]{listEntry});

        when(restTemplate.getForObject(any(String.class), any(Class.class)))
                .thenReturn(airQualityMockResponse);

        // Mock saving to the repository
        WaterQuality mockWaterQuality = new WaterQuality();
        mockWaterQuality.setId(1L);
        mockWaterQuality.setPH(7.5);
        mockWaterQuality.setTurbidity(2.0);
        mockWaterQuality.setWaterQualityIndex(12.5);

        when(waterQualityRepository.save(any(WaterQuality.class))).thenReturn(mockWaterQuality);

        // Act: Call the assessWaterQuality method
        // WaterQuality result = waterQualityService.assessWaterQuality(latitude, longitude);

        // Assert: Validate the result
     /*   assertNotNull(result);
        assertEquals(7.5, result.getPH()); // Example fallback value, assuming pH fetch failed
        assertEquals(2.0, result.getTurbidity()); // Derived value based on mock PM10
        assertEquals(12.5, result.getWaterQualityIndex()); // Calculated index
    }*/
    }
}
