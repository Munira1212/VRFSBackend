package com.example.vrfsbackend.controller;


import com.example.vrfsbackend.service.GroundwaterService;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class GroundwaterController {

    private final GroundwaterService groundwaterService;

    public GroundwaterController(GroundwaterService groundwaterService) {
        this.groundwaterService = groundwaterService;
    }

    @CrossOrigin(origins = "http://localhost:19006")
    @GetMapping("/groundwater/availability")
    public Map<String, Object> getGroundwaterAvailability() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("timestamp", LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        List<Map<String, Double>> coordinates = defineCoordinates();
        LocalDate startDate = LocalDate.now().minusDays(365);
        LocalDate endDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Map<String, Object>> locations = processLocations(coordinates, startDate, endDate, formatter);
        response.put("locations", locations);

        return response;
    }

    private List<Map<String, Object>> processLocations(List<Map<String, Double>> coordinates, LocalDate startDate, LocalDate endDate, DateTimeFormatter formatter) {
        List<Map<String, Object>> locations = new ArrayList<>();
        coordinates.forEach(coord -> {
            double latitude = coord.get("latitude");
            double longitude = coord.get("longitude");
            try {
                JSONObject weatherData = groundwaterService.fetchAPIData(buildWeatherApiUrl(latitude, longitude, startDate, endDate, formatter));
                if (weatherData.has("data")) {
                    double weatherChance = groundwaterService.analyzeWeatherData(weatherData);
                    JSONObject soilMoistureData = groundwaterService.fetchAPIData(buildSoilMoistureApiUrl(latitude, longitude, startDate, endDate, formatter));
                    double soilMoistureChance = groundwaterService.analyzeSoilMoistureData(soilMoistureData);

                    Map<String, Double> etResults = groundwaterService.calculateETScore(weatherData);
                    double combinedChance = calculateCombinedChance(weatherChance, soilMoistureChance, etResults.get("etScore"));

                    Map<String, Object> locationData = buildLocationData(latitude, longitude, combinedChance, weatherChance, soilMoistureChance, etResults);
                    locations.add(locationData);
                } else {
                    System.err.println("Error: 'data' key not found in weather data for coordinates: Latitude=" + latitude + ", Longitude=" + longitude);
                }
            } catch (Exception e) {
                System.err.println("Failed to process data for coordinates: " + coord + " due to: " + e.getMessage());
            }
        });
        return locations;
    }

    private List<Map<String, Double>> defineCoordinates() {
        return Arrays.asList(
                Map.of("latitude", 2.0473, "longitude", 45.3434),
                Map.of("latitude", 9.5600, "longitude", 44.0650),
                Map.of("latitude", 5.1521, "longitude", 46.1996),
                Map.of("latitude", 2.0386, "longitude", 45.3150),
                Map.of("latitude", 6.7543, "longitude", 46.6575),
                Map.of("latitude", 7.0421, "longitude", 45.3347),
                Map.of("latitude", 8.6354, "longitude", 47.3043),
                Map.of("latitude", 4.8604, "longitude", 47.6172),
                Map.of("latitude", 3.9736, "longitude", 47.2934),
                Map.of("latitude", 2.0321, "longitude", 45.8635),
                Map.of("latitude", 4.6150, "longitude", 46.7912),
                Map.of("latitude", 6.0345, "longitude", 46.1322),
                Map.of("latitude", 9.3200, "longitude", 42.9990)
        );
    }

    private String buildWeatherApiUrl(double latitude, double longitude, LocalDate startDate, LocalDate endDate, DateTimeFormatter formatter) {
        return String.format(Locale.US, "https://api.weatherbit.io/v2.0/forecast/daily?lat=%.6f&lon=%.6f&start_date=%s&end_date=%s&key=YOUR_API_KEY&units=metric",
                latitude, longitude, formatter.format(startDate), formatter.format(endDate));
    }

    private String buildSoilMoistureApiUrl(double latitude, double longitude, LocalDate startDate, LocalDate endDate, DateTimeFormatter formatter) {
        return String.format(Locale.US, "https://power.larc.nasa.gov/api/temporal/daily/point?parameters=GWETROOT,PRECTOT&community=AG&latitude=%.6f&longitude=%.6f&start=%s&end=%s&format=JSON",
                latitude, longitude, formatter.format(startDate).replace("-", ""), formatter.format(endDate).replace("-", ""));
    }

    private double calculateCombinedChance(double weatherChance, double soilMoistureChance, double etScore) {
        return 0.25 * weatherChance + 0.5 * soilMoistureChance + 0.25 * etScore;
    }

    private Map<String, Object> buildLocationData(double latitude, double longitude, double combinedChance, double weatherChance, double soilMoistureChance, Map<String, Double> etResults) {
        return Map.of(
                "latitude", latitude,
                "longitude", longitude,
                "availability", combinedChance,
                "recommendation", getRecommendation(combinedChance),
                "details", Map.of(
                        "weatherChance", weatherChance,
                        "soilMoistureChance", soilMoistureChance,
                        "etScore", etResults.get("etScore"),
                        "etAverage", etResults.get("etAverage")
                )
        );
    }

    private String getRecommendation(double combinedChance) {
        if (combinedChance >= 80) {
            return "High likelihood of groundwater: Recommend drilling.";
        } else if (combinedChance >= 60) {
            return "Moderate likelihood, some risk: Recommend further investigation.";
        } else {
            return "Low likelihood, high risk: Do not recommend drilling, suggest alternatives.";
        }
    }
}
