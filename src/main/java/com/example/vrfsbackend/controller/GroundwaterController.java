package com.example.vrfsbackend.controller;

import com.example.vrfsbackend.service.GroundwaterService;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController  // Make sure @RestController is used
public class GroundwaterController {

    private final GroundwaterService groundwaterService;

    public GroundwaterController() {
        this.groundwaterService = new GroundwaterService();
    }

    // Allow Cross-Origin requests from your frontend address
    @CrossOrigin(origins = "http://localhost:19006")
    @GetMapping("/groundwater/availability")
    public Map<String, Object> getGroundwaterAvailability() throws Exception {
        // Define a list of coordinates for analysis
        List<Map<String, Double>> coordinates = List.of(
                Map.of("latitude", 2.0473, "longitude", 45.3434),  // Mogadishu (Moderate Availability)
                Map.of("latitude", 9.5600, "longitude", 44.0650),  // Hargeisa (Moderate Availability)
                Map.of("latitude", 5.1521, "longitude", 46.1996),  // Kismayo (Moderate Availability)

                // Additional locations where groundwater is likely to be found (e.g., near rivers or lakes)
                Map.of("latitude", 2.0386, "longitude", 45.3150),  // Near river, likely high availability (e.g., near the Juba River)
                Map.of("latitude", 6.7543, "longitude", 46.6575),  // Near large water source, high availability (e.g., near major rivers)
                Map.of("latitude", 7.0421, "longitude", 45.3347),  // Moderate availability (semi-arid region)
                Map.of("latitude", 8.6354, "longitude", 47.3043),  // Low availability, dry region (desert area)

                // Areas where groundwater availability could be moderate to low
                Map.of("latitude", 4.8604, "longitude", 47.6172),  // Moderate availability (semi-arid)
                Map.of("latitude", 3.9736, "longitude", 47.2934),  // Moderate availability (semi-arid)
                Map.of("latitude", 2.0321, "longitude", 45.8635),  // Low availability, desert area
                Map.of("latitude", 4.6150, "longitude", 46.7912),  // Low availability, arid region
                Map.of("latitude", 6.0345, "longitude", 46.1322),  // High availability, near river or lakes
                Map.of("latitude", 9.3200, "longitude", 42.9990)   // Moderate availability, arid area
        );


        // Create a response structure
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("timestamp", LocalDate.now().toString()); // Add timestamp to the response

        // Prepare a list to hold results for each location
        List<Map<String, Object>> locations = new ArrayList<>();

        // Date formatting for API calls
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(365);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        // Loop through each location and fetch data
        for (Map<String, Double> coord : coordinates) {
            double latitude = coord.get("latitude");
            double longitude = coord.get("longitude");

            // Construct API URLs
            String weatherApiUrl = String.format(Locale.US,
                    "https://api.weatherbit.io/v2.0/forecast/daily?lat=%.6f&lon=%.6f&start_date=%s&end_date=%s&key=cff8af0488d347b892bfc434c9c48cce&units=metric",
                    latitude, longitude, startDateStr, endDateStr);


            String soilMoistureApiUrl = String.format(Locale.US,
                    "https://power.larc.nasa.gov/api/temporal/daily/point?parameters=GWETROOT,PRECTOT&community=AG&latitude=%.6f&longitude=%.6f&start=%s&end=%s&format=JSON",
                    latitude, longitude, startDateStr.replace("-", ""), endDateStr.replace("-", ""));

            // Fetch and analyze data
            JSONObject weatherData = groundwaterService.fetchWeatherData(weatherApiUrl);

            if (weatherData.has("data")) {
                double weatherChance = groundwaterService.analyzeWeatherData(weatherData); // Analyze weather data
                double soilMoistureChance = groundwaterService.analyzeSoilMoistureData(
                        groundwaterService.fetchSoilMoistureData(soilMoistureApiUrl)

                ); // Analyze soil moisture data

                // Fetch ET results from the service
                Map<String, Double> etResults = groundwaterService.calculateETScore(weatherData);

                // Combine the chances including ET score (you can adjust the weight of ET here)
                double weightWeather = 0.25;  // Weight for weatherChance
                double weightSoilMoisture = 0.5;  // Weight for soilMoistureChance
                double weightET = 0.25;  // Weight for etScore

                double combinedChance = (weightWeather * weatherChance + weightSoilMoisture * soilMoistureChance + weightET * etResults.get("etScore"));

                // Build the location result
                Map<String, Object> locationData = new HashMap<>();
                locationData.put("latitude", latitude);
                locationData.put("longitude", longitude);
                locationData.put("availability", combinedChance);
                locationData.put("recommendation", getRecommendation(combinedChance));
                locationData.put("details", Map.of(
                        "weatherChance", weatherChance,
                        "soilMoistureChance", soilMoistureChance,
                        "etScore", etResults.get("etScore"),
                        "etAverage", etResults.get("etAverage")
                ));

                // Add location data to the list
                locations.add(locationData);
            } else {
                System.out.println("Error: 'data' key not found in weather data for coordinates: Latitude=" + latitude + ", Longitude=" + longitude);
            }

        }

        // Add the processed locations to the response
        response.put("locations", locations);

        // Log the response to see what is being returned
        System.out.println("Response: " + response);

        // Return the structured response
        return response;
    }

    // Helper method to generate a recommendation
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

